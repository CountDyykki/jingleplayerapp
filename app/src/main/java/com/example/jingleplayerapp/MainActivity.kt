// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp

import CalendarViewModel
import Jingle
import Scheduler
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.jingleplayerapp.ui.theme.JingleplayerappTheme
import createjingles
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Int


data class Songtoplay(val name:String, val type:String)

class MainActivity : ComponentActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private val calendarViewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exoPlayer = ExoPlayer.Builder(this).build()
        // handle audiofocus
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, true)



        setContent {
            JingleplayerappTheme {
                Mainmenu(exoPlayer,calendarViewModel)
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }




}

@Composable
fun Mainmenu(exoPlayer: ExoPlayer, calendarViewModel: CalendarViewModel) {
    val nextsong = remember { mutableStateOf(Songtoplay("", "Start")) }
    val delayminutes = remember { mutableStateOf(5)}
    val jingleslist = remember { mutableStateListOf<Jingle>()}
    val jinglelength = remember { mutableStateOf(10) }
    val textit = remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                content =
                    {
                        Text("Select Calendar: ")
                        // LoadCalenderEvents(gameslist=gameslist)
                        CalendarDropdown(calendarViewModel = calendarViewModel)
                    })
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    SchedulerScreen(exoPlayer,jingleslist,nextsong,jinglelength,textit)
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
            // ShowCalenderEvents(gameslist)
            Configuretimes(jinglelength,delayminutes)
            // CreatePlayList(calendarViewModel,jingleslist,delayminutes)
            PlaylistScreen(calendarViewModel,jingleslist,delayminutes)
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
    val uiState by calendarViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Trigger data loading once when the composable enters the composition
        calendarViewModel.fetchCalendars(context)
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Row(
            modifier = Modifier.clickable {
                calendarViewModel.setDropdownExpanded(true)
            }
        ) {
            Text(text = uiState.selectedCalendar?.displayName ?: "No calendar selected")
        }
        DropdownMenu(
            expanded = uiState.isDropdownExpanded,
            onDismissRequest = { calendarViewModel.setDropdownExpanded(false) }
        ) {
            uiState.calendars.forEach { calendar ->
                DropdownMenuItem(
                    text = { Text(text = calendar.displayName) },
                    onClick = {
                        calendarViewModel.selectCalendar(calendar, context)
                    }
                )
            }
        }
    }
}

@Composable
fun PlaylistScreen(calendarViewModel: CalendarViewModel,playlist: MutableList<Jingle>,delayMinutes: MutableState<Int>
) {
    val uiState by calendarViewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    LaunchedEffect(uiState.games, delayMinutes.value) {
        while (true) {
            createjingles(uiState.games, playlist, delayMinutes)
            delay(1000) // Delay for 1 second
        }
    }

    // Use the playlist in your UI
    Column {
        playlist.forEach { jingle ->
            val starttime =  jingle.start.atZone(ZoneId.systemDefault()).format(formatter)
            Text(text = "$starttime | ${jingle.name} | ${jingle.type}")
        }
    }
}

@Composable
fun Configuretimes(jingleength:MutableState<Int>, delayminutes: MutableState<Int>){

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
                state =  jingleength ,
                range = 1..60,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }




}

@OptIn(UnstableApi::class)
@Composable
fun SchedulerScreen(exoPlayer:ExoPlayer, playlist: List<Jingle>, nextsong: MutableState<Songtoplay>, jinglelength: MutableState<Int>,textit:MutableState<String>){
    Scheduler(exoPlayer, playlist, nextsong, jinglelength,textit)
    Text(
        text = textit.value,
        modifier = Modifier.padding(16.dp)
    )
}