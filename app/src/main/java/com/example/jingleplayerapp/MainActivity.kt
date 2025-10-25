// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp

import AudioFilePickerScreen
import CalendarViewModel
import CalendarViewModelFactory
import SchedulerViewModel
import SchedulerViewModelFactory
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.jingleplayerapp.ui.theme.JingleplayerappTheme
import dagger.hilt.android.AndroidEntryPoint
import getFileName
import kotlinx.coroutines.delay
import mysuperMediaPlayer
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Int
import kotlin.getValue
import kotlin.text.get

@UnstableApi
data class UIstate(
    val minutesbeforeendgame: Int = 5,
    val jinglelength: Int = 10,
    val jingleuri: JingleUriState = JingleUriState()
)

data class Songtoplay(val name:String, val type:String)
// A data class to hold a URI and an optional name
data class JingleUri(val uri: Uri, val name: String)

// A data class to hold a set of JingleUri objects
@UnstableApi
data class JingleUriState(
    val audioStart: JingleUri=JingleUri( RawResourceDataSource.buildRawResourceUri(R.raw.start),"Default Start"),
    val audioPreEnd: JingleUri=JingleUri( RawResourceDataSource.buildRawResourceUri(R.raw.deltaend),"Default PreEnd"),
    val audioEnd: JingleUri=JingleUri( RawResourceDataSource.buildRawResourceUri(R.raw.end),"Default End"),
)

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JingleplayerappTheme {
                Mainmenu(this)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun Mainmenu(context: Context) {
    val application = LocalContext.current.applicationContext as Application

    // First, get the CalendarViewModel using its factory
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(application)
    )

    // Then, get the SchedulerViewModel using its factory and the CalendarViewModel instance
    val schedulerViewModel: SchedulerViewModel = viewModel(
        factory = SchedulerViewModelFactory(application, calendarViewModel)
    )
    var uiState by remember { mutableStateOf(UIstate())}
    //RunScheduler(uiState,schedulerViewModel,calendarViewModel)

    val calendarState = calendarViewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {

        schedulerViewModel.playbackEvent.collect { playbackData ->
            // Access the song and length from the single object
            val songToPlay = playbackData.song
            val jingleLength = playbackData.length
            val jinglemap = mapOf(
                "Start" to uiState.jingleuri.audioStart,
                "PreEnd" to uiState.jingleuri.audioPreEnd,
                "End" to uiState.jingleuri.audioEnd
            )
            val uri= jinglemap[playbackData.song.type]?.uri

            Log.i("Schedule player","Play callback")
             val exoPlayer = ExoPlayer.Builder(context).build()
            // handle audiofocus
            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                // .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                // ducking
                .setUsage(C.USAGE_ALARM)
                //.setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build()
            exoPlayer.setAudioAttributes(audioAttributes, true)



            if (uri!=null){
                Log.i("Schedule player","Start Playing ${uri}")
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
           /*     exoPlayer.addListener(object : Player.Listener {

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

*/

            delay(jingleLength.toLong() * 1000)
            exoPlayer.stop()
            }
            exoPlayer.release()
        }
    }


    // val calendarState = calendarViewModel.uiState.collectAsState()
    // val schedulerState = schedulerViewModel.uiState.collectAsState()


    LaunchedEffect(uiState) {
        schedulerViewModel.updateUiState(uiState)
    }


    // val playlist = remember { mutableStateListOf<Song>()}
    // val schedulerinfotxt = remember { mutableStateOf("") }
    //val nextsong = remember { mutableStateOf(Songtoplay("", "Start")) }

    var jingleUriState by remember {
        mutableStateOf(
            JingleUriState()
        )
    }
    Scaffold(
        topBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                content =
                    {
                        Text("Select Calendar: ")
                        CalendarDropdown(calendarViewModel = calendarViewModel)
                    })
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    SchedulerScreen(uiState,schedulerViewModel)
                })

        },
        floatingActionButton = {
            FAB()
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row() {
                AudioFilePickerScreen("Start", onAudioPicked = { uri ->
                    if (uri != null){
                    var filename=getFileName(context, uri)?:"Take Default"
                    jingleUriState = jingleUriState.copy(audioStart=JingleUri(uri,filename))}
                })
                Text(jingleUriState.audioStart.name)
            }
            Row() {
            AudioFilePickerScreen("PreEnd", onAudioPicked = { uri ->
                if (uri != null){
                var filename=getFileName( context,uri)?:"Take Default"

                jingleUriState = jingleUriState.copy(audioPreEnd =JingleUri(uri,filename))}
            })
                Text(jingleUriState.audioPreEnd.name)
            }
            Row() {
                AudioFilePickerScreen("End", onAudioPicked = { uri ->
                    if (uri != null){
                    var filename=getFileName( context,uri)?:"Take Default"
                    jingleUriState = jingleUriState.copy(audioEnd =JingleUri(uri,filename))
                }})
                Text(jingleUriState.audioEnd.name)
            }

            Configuretimes(uiState)
            JingleslistScreen(schedulerViewModel)
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
fun RunScheduler( uistate:UIstate,schedulerViewModel: SchedulerViewModel,calendarViewModel: CalendarViewModel){
    // Combine state into a single Flow
    Log.i("RunScheduler","Scheduler recomposed")

}


@Composable
fun FAB() {
    FloatingActionButton(onClick = { }) {
        Icon(
            painter = painterResource(R.drawable.play_arrow_24px),
            contentDescription = "toll",
        )
    }
}

@Composable
fun CalendarDropdown(calendarViewModel: CalendarViewModel) {
   val calenderuiState = calendarViewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) {
        // Trigger data loading once when the composable enters the composition
        calendarViewModel.fetchCalendars()
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Row(
            modifier = Modifier.clickable {
                calendarViewModel.setDropdownExpanded(true)
            }
        ) {
            Text(text = calenderuiState.selectedCalendar?.displayName ?: "No calendar selected")
        }
        DropdownMenu(
            expanded = calenderuiState.isDropdownExpanded,
            onDismissRequest = { calendarViewModel.setDropdownExpanded(false) }
        ) {
            calenderuiState.calendars.forEach { calendar ->
                DropdownMenuItem(
                    text = { Text(text = calendar.displayName) },
                    onClick = {
                        calendarViewModel.selectCalendar(calendar)
                    }
                )
            }
        }
    }
}

@Composable
fun JingleslistScreen(schedulerViewModel: SchedulerViewModel) {
    val schedulerstate by schedulerViewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    // show the playlist from the scheduler
    Column {
        schedulerstate.playlist.forEach { jingle ->
            val starttime =  jingle.start.atZone(ZoneId.systemDefault()).format(formatter)
            Text(text = "$starttime | ${jingle.name} | ${jingle.type}")
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun Configuretimes(uistate: UIstate){

    // local state variables
    var jinglelength=remember {mutableStateOf(10)}
    var delayminutes = remember {mutableStateOf(5)}

    // are copied over to the global uistate class
    uistate.copy(minutesbeforeendgame = delayminutes.value)
    uistate.copy(jinglelength = jinglelength.value)

    Row {
        Text("Min  end")
        Box(modifier = Modifier.fillMaxSize(0.2f)) {
            NumberPicker(
                // state = remember { mutableStateOf(5) },
                state =  delayminutes ,
                range = 0..30,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text("jingle sec")
        Box(modifier = Modifier.fillMaxSize(0.2f)) {
            NumberPicker(
                // state = remember { mutableStateOf(5) },
                state =  jinglelength ,
                range = 1..60,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }




}

@OptIn(UnstableApi::class)
@Composable
fun SchedulerScreen( uiState: UIstate,schedulerViewModel: SchedulerViewModel){
    val schedulerstate by schedulerViewModel.uiState.collectAsState()
    Text(
        text = schedulerstate.infotxt,
        modifier = Modifier.padding(16.dp)
    )
}

