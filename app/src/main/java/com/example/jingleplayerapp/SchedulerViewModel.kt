import android.app.Application
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.jingleplayerapp.UIstate
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
data class SchedulerUiState(
    // A List of the Songs which are still to play
    val playlist: List<Song> = emptyList(),
    // the next jingle to play
    val nextsong: Song?=null,
    // time when the next jingle will be played
    val nextsongin: Duration?=null,
    // start playing with this boolean
    val isplaying: Boolean = false,
    val infotxt: String=""
)


@UnstableApi

class SchedulerViewModelFactory(
    private val application: Application,
    private val calendarViewModel: CalendarViewModel // Correctly passing the dependency
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchedulerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchedulerViewModel(application, calendarViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
    @UnstableApi
    class SchedulerViewModel(
        application: Application,
        private val calendarViewModel: CalendarViewModel
    ) : AndroidViewModel(application) {


        private val _playbackEvent = MutableSharedFlow<PlaybackData>()

        // Make it public as a SharedFlow
        val playbackEvent: SharedFlow<PlaybackData> = _playbackEvent.asSharedFlow()

    private val _uiStatesched = MutableStateFlow(SchedulerUiState())
    private val _uiState = MutableStateFlow(UIstate())

    val uiState: StateFlow<SchedulerUiState> = _uiStatesched.asStateFlow()
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
    @OptIn(UnstableApi::class)
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

                     // first we create a playlist from the songs still to play
                    val fullplaylist = createplaylist(calendaruistate.games,uiState.minutesbeforeendgame)
                    val upcomingPlaylist: List<Song> = emptyList()
                    if (fullplaylist.isEmpty()) {
                        _uiStatesched.update { it.copy(infotxt = "Empty Playlist. Consider loading a calendar") }
                    }
                    else{
                        val now = Instant.now()
                        val threshold = Duration.ofMillis(500)
                        val _actualsongfilterlist = fullplaylist.filter { song ->
                            val duration = Duration.between(now, song.start).abs()
                            duration.compareTo(threshold) <= 0
                        }
                        val _actualsong = _actualsongfilterlist.firstOrNull()
                        // list with only the remaining songs:
                        val upcomingPlaylist=fullplaylist.filter {it.start>now}
                        val _nextsong = fullplaylist.firstOrNull { it.start > now }
                        // Calculate time difference between now and the next song to play

                        _nextsong?.let {
                            Log.i("Scheduler Val","Next song: ${_nextsong.name} | ${_nextsong.type} |${_nextsong.start} ")
                            var _nextsongin = Duration.between(now, _nextsong.start)
                        _uiStatesched.update { it.copy(
                            playlist= upcomingPlaylist,
                            nextsongin = _nextsongin,
                            nextsong=_nextsong,
                            infotxt = "${ _nextsong.name} | ${ _nextsong.type} in ${_nextsongin.toMinutes()}:${_nextsongin.toSecondsPart()} min")
                        }
                        }
                        // wenn die playlist einen weiter springt spielen wir den vorigen song ab.




                        _actualsong?.let {
                            Log.i("Scheduler Val","Actual Song: ${_actualsong.name} | ${_actualsong.type} | ${_actualsong.start}")

                                _uiStatesched.update { it.copy(isplaying = true) }
                                Log.i("Schedule Player", "Playing ${ _actualsong.name} | ${ _actualsong.type} ")
                                val playbackData = PlaybackData(song=_actualsong, length = uiState.jinglelength)
                                // Emit the single object to the SharedFlow
                                _playbackEvent.emit(playbackData)



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
        Log.i("Scheduler Playlist", "calendar has ${gamesList.size}")
        var playlist: MutableList<Song> = mutableListOf()
        for (game in gamesList) {
            val now = Instant.now()
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
            //if (now.isBefore(songDeltaEnd.start) && minutesbeforeendgame > 0) {
                playlist.add(songDeltaEnd)
            // }
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

class mysuperMediaPlayer(application:Application){
    val exoPlayer = ExoPlayer.Builder(application).build()
    init {
        // Handle audio focus and playback when player state changes
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                exoPlayer.release()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // Stop playback and release focus when done
                    exoPlayer.stop()
                    exoPlayer.release()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // Handle playback errors and stop the player
                exoPlayer.stop()
                exoPlayer.release()
            }
        })

        // Set audio attributes with auto-focus handling
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, true) // Pass true for auto-focus
    }


    fun playAudio(uri: Uri?) {
        if (uri != null) {
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun stop() {
        Log.i("Musicplayer", "Stop playing.")
        exoPlayer.stop()
        exoPlayer.release()

    }

}