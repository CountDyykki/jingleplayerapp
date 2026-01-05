// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp

import CalendarDropdown
import CalendarViewModel
import CalendarViewModelFactory

import PlaybackService
import SchedulerViewModel
import SchedulerViewModelFactory
import SelectJinglesScreen
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.BottomAppBar

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.example.jingleplayerapp.ui.theme.JingleplayerappTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Int

data class JingleUri(val uri: Uri, val name: String)

data class JingleUriState(
    val audioStart: JingleUri,
    val audioPreEnd: JingleUri,
    val audioEnd: JingleUri
) {
    companion object {
        fun create(context: Context): JingleUriState {
            return JingleUriState(
                audioStart = JingleUri(
                    Uri.parse("android.resource://${context.packageName}/${R.raw.start}"),
                    "Default Start"
                ),
                audioPreEnd = JingleUri(
                    Uri.parse("android.resource://${context.packageName}/${R.raw.deltaend}"),
                    "Default PreEnd"
                ),
                audioEnd = JingleUri(
                    Uri.parse("android.resource://${context.packageName}/${R.raw.end}"),
                    "Default End"
                )
            )
        }
    }
}

data class UIstate(
    var minutesbeforeendgame: Int,
    var jinglelength: Int,
    var jingleuri: JingleUriState,
    var pausestate: Boolean
){
    companion object {
        fun create(context: Context): UIstate {
            return UIstate(
                minutesbeforeendgame = 5,
                jinglelength = 5,
                jingleuri = JingleUriState.create(context),
                pausestate = false
            )
        }
    }
}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JingleplayerappTheme {
                Mainmenu()
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(UnstableApi::class)
@Composable
fun Mainmenu() {
    val application = LocalContext.current.applicationContext as Application
    // instantiate the uistate
    val jingleUriState = remember { mutableStateOf(JingleUriState.create(application)) }
    val uiState = remember { mutableStateOf(UIstate.create(application)) }
    uiState.value.jingleuri = jingleUriState.value
    // var jingleUriState by remember { mutableStateOf(JingleUriState.create(application)) }
    // First, get the CalendarViewModel using its factory
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(application)
    )
    // Then, get the SchedulerViewModel using its factory and the CalendarViewModel instance
    val schedulerViewModel: SchedulerViewModel = viewModel(
        factory = SchedulerViewModelFactory(application, calendarViewModel)
    )
    // when the uistate changes activate the flow in the scheduler view model
    LaunchedEffect(uiState.value) {
        schedulerViewModel.updateUiState(uiState.value)
        Log.i("Launched Effect", "Launcher uistate minutesbeforeendgame is ${uiState.value.minutesbeforeendgame}")
    }

    // collect playback event from scheduler
    LaunchedEffect(Unit) {
        schedulerViewModel.playbackEvent.collect { playbackData ->
            // Access the song and length from the single object
            // val songToPlay = playbackData.song
            val jingleLength = playbackData.length
            val jinglemap = mapOf(
                "Start" to uiState.value.jingleuri.audioStart,
                "PreEnd" to uiState.value.jingleuri.audioPreEnd,
                "End" to uiState.value.jingleuri.audioEnd
            )
            val playbackservice = PlaybackService(application)
            val uri = jinglemap[playbackData.song.type]?.uri
            Log.i("Schedule player", "uri is $uri")
            if (uri != null) {
                Log.i("Schedule player", "Start Playing $uri")
                playbackservice.playjingle(uri)
                Log.i("Schedule player", "Delaying stop by $jingleLength")
                if (jingleLength.toLong()> 0) {
                    Log.i("Schedule player", "Delaying stop")
                    delay(jingleLength.toLong() * 1000)
                    playbackservice.stopplaying()
                 }
            }
        }
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
                    SchedulerScreen(schedulerViewModel)
                })

        },
        floatingActionButton = {
            FAB(uiState)
        }
    ) { innerPadding ->
        Column(modifier=Modifier.padding(innerPadding)){
        SelectJinglesScreen(jingleUriState)
        HorizontalDivider(thickness = 1.dp)
        Configuretimes(uiState)
        HorizontalDivider(thickness = 1.dp)
        PlaylistScreen(schedulerViewModel)
        }}
    }


@Composable
fun FAB(uistate: MutableState<UIstate>) {
    FloatingActionButton(
        onClick = {uistate.value=uistate.value.copy( pausestate = !uistate.value.pausestate)}
    ) {
        if (uistate.value.pausestate){
        Icon(
            painter = painterResource(R.drawable.volume_off_24px),
            contentDescription = "Muted",
        )}
        else{
            Icon(
                painter = painterResource(R.drawable.volume_mute_24px),
                contentDescription = "Unmuted",
            )}
        }
    }


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreen(schedulerViewModel: SchedulerViewModel) {
    val schedulerstate by schedulerViewModel.schedState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    // show the playlist from the scheduler
    Text("Playlist", fontWeight = FontWeight.Bold)
    Column(modifier = Modifier.fillMaxSize()) {
        schedulerstate.playlist.forEach { jingle ->
            val starttime =  jingle.start.atZone(ZoneId.systemDefault()).format(formatter)
            Text(text = "$starttime | ${jingle.name} | ${jingle.type}")
        }

    }
}


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(UnstableApi::class)
@Composable
fun SchedulerScreen( schedulerViewModel: SchedulerViewModel){
    val schedulerstate by schedulerViewModel.schedState.collectAsState()
    Text(
        text = schedulerstate.infotxt,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun Configuretimes(uistate: MutableState<UIstate>) {

    // are copied over to the global uistate class
    val minutesbeforeendgame = remember { mutableIntStateOf(uistate.value.minutesbeforeendgame) }
    val jinglelength = remember { mutableIntStateOf(uistate.value.jinglelength) }
    Log.i("Configure Times", "delayminutes: $minutesbeforeendgame linglelength: $jinglelength")
    uistate.value = uistate.value.copy(
        minutesbeforeendgame = minutesbeforeendgame.intValue,
        jinglelength = jinglelength.intValue
    )
    Text(text="Timing",fontWeight= FontWeight.Bold)
    Row{
        Column {
            Text( "PreEnd Start time (min)")
            Text( "Jingle duration (sec)")
        }
        Column {
            NumberPickerHorizontal(
                state = minutesbeforeendgame,
                modifier = Modifier,
                range = 0..30,
                textStyle = LocalTextStyle.current,
                onStateChanged = { minutesbeforeendgame.intValue = it },
            )
            NumberPickerHorizontal(
                state = jinglelength,
                modifier = Modifier,
                range = 0..60,
                textStyle = LocalTextStyle.current,
                onStateChanged = { jinglelength.intValue = it },
            )
        }
    }
}