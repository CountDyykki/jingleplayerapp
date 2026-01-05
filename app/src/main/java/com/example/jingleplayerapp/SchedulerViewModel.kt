import android.app.Application
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.jingleplayerapp.UIstate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlin.collections.List


// Ein Song ist ein Eintrag in der Playlist
data class Song(val name: String, val type: String, val start: Instant)
data class PlaybackData(val song: Song, val length: Int)

// Das ist die State-Klasse die dieses viewmodel bereitstellt.
data class SchedulerState(
    // A List of the Songs which are still to play
    val playlist: List<Song> = emptyList(),
    // the next jingle to play
    val nextsong: Song?=null,
    // time when the next jingle will be played
    val nextsongin: Duration?=null,
    // infotxt of schedulerstate
    val infotxt: String=""
)


@UnstableApi

class SchedulerViewModelFactory(
    private val application: Application,
    private val calendarViewModel: CalendarViewModel // Correctly passing the dependency
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchedulerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchedulerViewModel(application, calendarViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
    @RequiresApi(Build.VERSION_CODES.S)
    @UnstableApi
    class SchedulerViewModel(
        application: Application,
        private val calendarViewModel: CalendarViewModel
    ) : AndroidViewModel(application) {


        private val _playbackEvent = MutableSharedFlow<PlaybackData>()

        // Make it public as a SharedFlow
        val playbackEvent: SharedFlow<PlaybackData> = _playbackEvent.asSharedFlow()

    private val _schedState = MutableStateFlow(SchedulerState())
    private val _uiState = MutableStateFlow(UIstate.create(application))

    val schedState: StateFlow<SchedulerState> = _schedState
    private var schedulingJob: Job? = null

    init {
        // Start the scheduling job when the ViewModel is created
        scheduleJingles()
    }

    // Public function for the composable to send UIstate updates
    @OptIn(UnstableApi::class)
    fun updateUiState(newState: UIstate) {
        _uiState.value = newState
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(UnstableApi::class, ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class,
        ExperimentalCoroutinesApi::class
    )
    fun scheduleJingles(){
        Log.i("SchedulerJob","Starting job")
        schedulingJob?.cancel()
        schedulingJob = viewModelScope.launch {
            combine(_uiState, calendarViewModel.uiState, tickerFlow()) { uiState, calendarUiState, _ ->
                uiState to calendarUiState
            }
                // Combine the configuration flow with the ticker flow
                .flatMapLatest { (uiState,calendaruistate) ->
                flow<Unit> {
                    Log.i("SchedulerJob", "Scheduling Step.")
                    Log.i("Scheduler","minutesbeforeendgame: ${uiState.minutesbeforeendgame}")
                     // first we create a playlist from the songs still to play
                    val fullplaylist = createplaylist(calendaruistate.games,uiState.minutesbeforeendgame)
                    // val upcomingPlaylist: List<Song> = emptyList()
                    if (fullplaylist.isEmpty()) {
                        _schedState.update { it.copy(infotxt = "Empty Playlist\nConsider selecting a calendar") }
                    }
                    else{
                        val now = Instant.now()
                        val threshold = Duration.ofMillis(500)
                        val actualsongfilterlist = fullplaylist.filter { song ->
                            val duration = Duration.between(now, song.start).abs()
                            duration.compareTo(threshold) <= 0
                        }
                        val actualsong = actualsongfilterlist.firstOrNull()
                        // list with only the remaining songs:
                        val upcomingPlaylist=fullplaylist.filter {it.start>now}
                        val nextsong = fullplaylist.firstOrNull { it.start > now }
                        // Calculate time difference between now and the next song to play

                        nextsong?.let {
                            Log.i("Scheduler Val","Next song: ${nextsong.name} | ${nextsong.type} |${nextsong.start} ")
                            val nextsongin = Duration.between(now, nextsong.start)
                        _schedState.update { it.copy(
                            playlist= upcomingPlaylist,
                            nextsongin = nextsongin,
                            nextsong=nextsong,
                            infotxt = "${ nextsong.name} | ${ nextsong.type} in ${nextsongin.toMinutes()}:${nextsongin.toSecondsPart()} min")
                        }
                        }
                        // wenn die playlist einen weiter springt spielen wir den vorigen song ab.
                        actualsong?.let {
                            // aber nur wenn wir nicht gerade pause gedrückt haben
                            if (! _uiState.value.pausestate){
                            Log.i("Scheduler Val","Actual Song: ${actualsong.name} | ${actualsong.type} | ${actualsong.start}")
                                Log.i("Schedule Player", "Playing ${ actualsong.name} | ${ actualsong.type} ")
                                val playbackData = PlaybackData(song=actualsong, length = uiState.jinglelength)
                                // Emit the single object to the SharedFlow
                                _playbackEvent.emit(playbackData)
                            }
                        }
                    }
                }
            }
                .collect()
        }
    }
    private fun tickerFlow(interval: Long = 1000) = flow {
        while (true) {
            emit(Unit) // Emits a value to trigger the combination
            delay(interval)
        }
    }

    private fun createplaylist(gamesList:List<Game>,minutesbeforeendgame:Int):List<Song> {
        val playlist: MutableList<Song> = mutableListOf()
        for (game in gamesList) {
            // val now = Instant.now()
            val songStart = Song(game.name, "Start", game.start)
            val songEnd = Song(game.name, "End", game.end)
            val eventDeltaEndDateTime = game.end.minusSeconds(minutesbeforeendgame.toLong() * 60)
            val songDeltaEnd = Song(game.name, "PreEnd", eventDeltaEndDateTime)

           // if (now.isBefore(songStart.start)) {
               playlist.add(songStart)
           //  }
            // if (now.isBefore(songEnd.start)) {
                playlist.add(songEnd)
            // }
            if (minutesbeforeendgame > 0) {
                playlist.add(songDeltaEnd)
             }
        }
        playlist.sortBy { it.start }
        // update the uiState
        Log.i("CreatePlayList", "${playlist.size} jingles left in Playlist")
        // _uiStatesched.update { it.copy(
        //    playlist = playlist
        //) }
        return playlist
    }
}
