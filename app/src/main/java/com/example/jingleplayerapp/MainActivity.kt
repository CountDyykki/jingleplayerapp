// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp

import CalendarViewModel
import Game
import Jingle
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.jingleplayerapp.ui.theme.JingleplayerappTheme
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


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
    val jingleslist = remember { mutableStateListOf<Jingle>() }
    val nextsong = remember { mutableStateOf(Songtoplay("", "Start")) }
    val delayminutes = remember { mutableStateOf(5) }
    val jinglelength = remember { mutableStateOf(10) }
    Scaffold(
        topBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                content =
                    {
                        Text("Choose Calendar")
                        // LoadCalenderEvents(gameslist=gameslist)
                        CalendarDropdown(calendarViewModel = calendarViewModel)
                    })
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    Scheduler(exoPlayer,jingleslist,nextsong,jinglelength)
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
            CreatePlayList(calendarViewModel,jingleslist,delayminutes)
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
fun CreatePlayList(calendarViewModel: CalendarViewModel, playlist: MutableList<Jingle>, delayminutes:MutableState<Int>) {
    val uiState by calendarViewModel.uiState.collectAsState()
    val gameslist = uiState.games
    Log.i("CreatePlayList","Create Jingle list with ${gameslist.size} game(s) and delay of ${delayminutes}" )


    // LaunchedEffect(gameslist) {
    //   while (true) {
    playlist.clear()
    for (index in gameslist.indices) {
        val now = Instant.now()
        val game: Game = gameslist[index]

        val jinglestart = Jingle(game.name, "Start", game.start)
        val jingleend = Jingle(game.name, "End", game.end)
        val enventdeltaenddatetime =
            game.end.minusSeconds(delayminutes.value.toLong() * 60)
        val jingledeltaend =
            Jingle(game.name, "DeltaEnd", enventdeltaenddatetime)

        if (now.isBefore(jinglestart.start)) {
            playlist.add(jinglestart)
        }
        if (now.isBefore(jingleend.start)) {
            playlist.add(jingleend)
        }
        if (now.isBefore(jingledeltaend.start)) {
            if (delayminutes.value.toLong() > 0) {
                playlist.add(jingledeltaend)
            }
        }
        playlist.sortBy { it.start }
        Log.i("CreatePlayList", "${playlist.size} jingles added to Playlist")
        //     delay(1000)

        // }


        // }
    }

    Text(
        text = "Songs still to play:",
        // modifier = Modifier.padding(16.dp)
    )
    LazyColumn( modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.9f)
    ) {
        items(playlist) { jingle ->
            Text(text = jingle.name + " " + jingle.type + " "+ LocalDateTime.ofInstant(jingle.start,
                ZoneId.systemDefault()), modifier = Modifier.padding(8.dp))
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
fun Scheduler(exoPlayer:ExoPlayer, playlist: List<Jingle>, nextsong: MutableState<Songtoplay>, jinglelength: MutableState<Int>) {
    var startPlaying = remember { mutableStateOf<Boolean>(false) }
    Log.i("FindNextSong", "Starting Scheduler with ${playlist.size} jingles")
    val songtoplay = remember {mutableStateOf<Songtoplay>(Songtoplay("","Start"))}
    var textit by remember { mutableStateOf("") }
    //LaunchedEffect(Unit) {
    //    while(true){
    if (playlist.isNotEmpty()) {
        val now = Instant.now()
        val nextjingle = playlist.first()
        val duration =Duration.between(now,nextjingle.start)
        nextsong.value = Songtoplay(nextjingle.name, nextjingle.type)
        textit = "${nextjingle.name}/${nextjingle.type}: Start in ${duration.toMinutes()} min."
        Log.i(
            "FindNextSong",
            "First jingle with positive duration: ${nextjingle.name}, Time to start: ${duration.toMinutes()} minutes"
        )
        // wenn die playlist einen weiterspringt
        if (songtoplay.value != nextsong.value  ){
            // spiele den song davor einmal ab
            Log.i("PlaySong", "Playing ${songtoplay.value.type}-jingle for ${songtoplay.value.name} ")
            PlaySong( exoPlayer, songtoplay,jinglelength)
            songtoplay.value=nextsong.value
        }
    } else {
        Log.i("FindNextSong", "No jingle with positive duration found.")
        textit = "No jingles left"
    }

    //      delay(10000)
    //  }
    //}
    Text(
        text = textit,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun PlaySong( exoPlayer: ExoPlayer, songtoplay: MutableState<Songtoplay>,jinglelength: MutableState<Int>){
    Log.i("Playertask","PlaySong started")
    val soundMap = mutableMapOf(
        "Start" to R.raw.start,
        "End" to R.raw.end,
        "DeltaEnd" to R.raw.beforeend
    )
    val soundId=soundMap[songtoplay.value.type]
    if (soundId!=null) {
        LaunchedEffect(Unit) {
            Log.i("Playertask","Playing song ${soundId} with delay of ${jinglelength.value}")
            val rawResourceUri = RawResourceDataSource.buildRawResourceUri(soundId)
            val mediaItem = MediaItem.fromUri(rawResourceUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            Log.i("PlayerTask", "${exoPlayer.playbackState}")
            delay(jinglelength.value.toLong()*1000)
            Log.i("Playertask","Stopping Exoplayer")
            exoPlayer.stop()
        }
    }
}