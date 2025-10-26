// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp

import AudioFilePickerScreen
import CalendarViewModel
import CalendarViewModelFactory
import PlaybackService
import SchedulerViewModel
import SchedulerViewModelFactory
import SimpleNumberPicker
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import com.example.jingleplayerapp.ui.theme.JingleplayerappTheme
import dagger.hilt.android.AndroidEntryPoint
import getFileName
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Int

@UnstableApi
data class UIstate(
    var minutesbeforeendgame: Int = 5,
    var jinglelength: Int = 10,
    var jingleuri: JingleUriState = JingleUriState()
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
    var uiState = remember { mutableStateOf(UIstate()) }
    var jingleUriState by remember { mutableStateOf(JingleUriState()) }
    Log.i("Main Menu", "uistate minutesbeforeendgame is ${uiState.value.minutesbeforeendgame}")
    LaunchedEffect(Unit) {
        schedulerViewModel.playbackEvent.collect { playbackData ->
            // Access the song and length from the single object
            val songToPlay = playbackData.song
            val jingleLength = playbackData.length
            val jinglemap = mapOf(
                "Start" to uiState.value.jingleuri.audioStart,
                "PreEnd" to uiState.value.jingleuri.audioPreEnd,
                "End" to uiState.value.jingleuri.audioEnd
            )
            val playbackservice = PlaybackService(application)
            val uri = jinglemap[playbackData.song.type]?.uri
            Log.i("Schedule player", "uri is ${uri}")
            if (uri != null) {
                Log.i("Schedule player", "Start Playing ${uri}")
                playbackservice.playjingle(uri)
                Log.i("Schedule player", "Delaying stop by ${jingleLength}")
                if (jingleLength.toLong()> 0) {
                    Log.i("Schedule player", "Delaying stop")
                    delay(jingleLength.toLong() * 1000)
                    playbackservice.stopplaying()
                 }
            }
        }
    }

    LaunchedEffect(uiState.value) {
        schedulerViewModel.updateUiState(uiState.value)
        Log.i("Launched Effect", "Launcher uistate minutesbeforeendgame is ${uiState.value.minutesbeforeendgame}")
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
            FAB()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AudioFilePickerScreen("Start", onAudioPicked = { uri ->
                    if (uri != null) {
                        var filename = getFileName(context, uri) ?: "Take Default"
                        jingleUriState = jingleUriState.copy(audioStart = JingleUri(uri, filename))
                    }
                })
                Text(jingleUriState.audioStart.name)
            }
            Row(verticalAlignment = Alignment.CenterVertically ) {
                AudioFilePickerScreen("PreEnd", onAudioPicked = { uri ->
                    if (uri != null) {
                        var filename = getFileName(context, uri) ?: "Take Default"

                        jingleUriState = jingleUriState.copy(audioPreEnd = JingleUri(uri, filename))
                    }
                })
                Text(jingleUriState.audioPreEnd.name)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AudioFilePickerScreen("End", onAudioPicked = { uri ->
                    if (uri != null) {
                        var filename = getFileName(context, uri) ?: "Take Default"
                        jingleUriState = jingleUriState.copy(audioEnd = JingleUri(uri, filename))
                    }
                })
                Text(jingleUriState.audioEnd.name)
            }

            HorizontalDivider(thickness = 1.dp)
            Configuretimes(uiState)
            HorizontalDivider(thickness = 1.dp)
            JingleslistScreen(schedulerViewModel)
        }
    }
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
    val schedulerstate by schedulerViewModel.schedState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    // show the playlist from the scheduler
    Text("Playlist", fontWeight = FontWeight.Bold)
    Column {
        schedulerstate.playlist.forEach { jingle ->
            val starttime =  jingle.start.atZone(ZoneId.systemDefault()).format(formatter)
            Text(text = "$starttime | ${jingle.name} | ${jingle.type}")
        }

    }
}

@OptIn(UnstableApi::class)
@Composable
fun Configuretimes(uistate: MutableState<UIstate>) {

    // local state variables


    // are copied over to the global uistate class
    var delayminutes = remember { mutableIntStateOf(5) }
    var jinglelength = remember { mutableIntStateOf(10) }
    Log.i("Configure Times", "delayminutes: ${delayminutes} linglelength: ${jinglelength}")
    uistate.value = uistate.value.copy(
        minutesbeforeendgame = delayminutes.value,
        jinglelength = jinglelength.value
    )

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)){
        SimpleNumberPicker(
                label = { it.toString() },
                range = 0..30,
                onValueChange = { delayminutes.value = it },
                value = delayminutes.value,
                description = "PreEnd (min)"
            )
        SimpleNumberPicker(
            label = { it.toString() },
            range = 0..30,
            onValueChange = { jinglelength.value = it },
            value = jinglelength.value,
            description = "Jingle Length (sec)"
        )
        }
    }






@OptIn(UnstableApi::class)
@Composable
fun SchedulerScreen( schedulerViewModel: SchedulerViewModel){
    val schedulerstate by schedulerViewModel.schedState.collectAsState()
    Text(
        text = schedulerstate.infotxt,
        modifier = Modifier.padding(16.dp)
    )
}

