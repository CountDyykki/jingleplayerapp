// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp
import android.content.Context
import android.util.Log
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import java.time.Duration
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.FloatingActionButton

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text

import android.media.MediaPlayer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.jingleplayerapp.ui.theme.JingleplayerappTheme
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


data class Jingle(val name: String, val type: String, val start: LocalDateTime)
data class Game(val name: String,  val start: LocalDateTime,val end: LocalDateTime)

data class Nextsong(val name:String, val type: String, val duration: Duration)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    lateinit var mediaPlayer: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JingleplayerappTheme {
                Mainmenu(this)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun Mainmenu(context: Context) {
    var delayminutes = remember { mutableStateOf("5") }
    val jingleslist = remember { mutableStateListOf<Jingle>() }
    val gameslist =  remember {mutableStateListOf<Game>()}
    Scaffold(
        topBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                content =
                    {
                        Text("Choose Calendar")
                        LoadCalenderEvents(gameslist=gameslist)
                    })
        },
        bottomBar = {
            var nextsong = remember {mutableStateOf<Nextsong>(Nextsong("","Start", Duration.ZERO))}
            BottomAppBar(
                content = {
                    FindNextSong(jingleslist,nextsong)
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
            ShowCalenderEvents(gameslist)
            CreatePlayList(gameslist,jingleslist,delayminutes)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoadCalenderEvents(gameslist:MutableList<Game>) {
    Log.i("LoadCalenderEvents", "Fire Composable")
    val calNamestate = remember { mutableStateOf("") }
    val calIdstate = remember { mutableLongStateOf(-1) }

    val context = LocalContext.current
    val isDropDownExpanded = remember {
        mutableStateOf(false)
    }

    val itemPosition = remember {
        mutableStateOf(0)
    }

    var calnames =  remember {mutableListOf<String>("Select Calender")}
    var calmap =  remember {mutableMapOf<String, Long>()}





    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    Log.i("LoadCalenderEvents", "filling drop down menu")
                    isDropDownExpanded.value = true
                    // Read all the calenders from the calendar app
                    val CALENDAR_PROJECTION = arrayOf(
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                        // CalendarContract.Calendars.NAME,
                        // CalendarContract.Calendars.CALENDAR_COLOR,
                    )
                    val PROJECTION_ID_INDEX = 0
                    val PROJECTION_DISPLAY_NAME_INDEX = 1
                    // val PROJECTION_NAME_INDEX = 2
                    // val PROJECTION_CALENDAR_COLOR_INDEX = 3
                    val uri = CalendarContract.Calendars.CONTENT_URI
                    val selection = ""
                    val selectionArgs = emptyArray<String>()
                    val calendarscursor: Cursor? = context.contentResolver.query(
                        uri,
                        CALENDAR_PROJECTION,
                        selection, selectionArgs,
                        null,
                    )
                    calendarscursor?.apply {
                        while (moveToNext()) {
                            var displayname = getString(PROJECTION_DISPLAY_NAME_INDEX)
                            var id = getLong(PROJECTION_ID_INDEX)
                            calnames.add(displayname)
                            calmap.put(displayname, id)
                        }
                    }
                    Log.i("LoadCalenderEvents", "Load Events")


            }

            ) {
                Text(text = calnames[itemPosition.value])

            }
            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {

                    isDropDownExpanded.value = false
                }
            ) {
                calnames.forEachIndexed { index, calname ->
                    DropdownMenuItem(
                        text = {
                            Text(text = calname)
                        },
                        onClick = {
                            isDropDownExpanded.value = false
                            itemPosition.value = index
                            calNamestate.value = calname
                            // get Calendar ID from map
                            calIdstate.value = calmap.getValue(calname)
                            Log.i("LoadCalenderEvents", "Calender is ${calname} with ID ${calIdstate.value}")
                            // Read the Calender Events
                            val EVENTPROJECTION = arrayOf(
                                CalendarContract.Events.TITLE,
                                CalendarContract.Events.DTSTART,
                                CalendarContract.Events.DTEND,
                             //   CalendarContract.Events.EVENT_TIMEZONE,
                             //   CalendarContract.Events.EVENT_END_TIMEZONE
                            )
                            val EVENTPROJECTION_TITLE_INDEX = 0
                            val EVENTPROJECTION_DTSTART_INDEX = 1
                            val EVENTPROJECTION_DTEND_INDEX = 2
                            // val EVENTPROJECTION_TZ_INDEX= 3
                            // val EVENTPROJECTION_TZEND_INDEX= 4
                            var calId = calIdstate.value.toString()
                            val urievent = CalendarContract.Events.CONTENT_URI
                            val selectionevent: String = "${CalendarContract.Events.CALENDAR_ID}=${calId}"
                            val eventscursor: Cursor? = context.contentResolver.query(
                                urievent,
                                EVENTPROJECTION,
                                selectionevent,
                                null,
                                null
                            )
                            when (eventscursor?.count) {
                                null -> {
                                    Log.e("LoadCalenderEvents", "empty cursor")
                                }

                                0 -> {
                                    Log.i("LoadCalenderEvents", "0 cursor")
                                }

                                else -> {
                                    Log.i("LoadCalenderEvents", "working cursor")
                                    gameslist.clear()
                                    eventscursor.apply {
                                        while (moveToNext()) {
                                            val eventtitle = getString(EVENTPROJECTION_TITLE_INDEX)
                                            val eventstart = getLong(EVENTPROJECTION_DTSTART_INDEX)
                                            val eventend = getLong(EVENTPROJECTION_DTEND_INDEX)
                                            //val eventtz = getString(EVENTPROJECTION_TZ_INDEX)
                                            //val eventtzend = getString(EVENTPROJECTION_TZEND_INDEX)

                                            val eventstartdatetime = Instant.ofEpochMilli(eventstart)
                                                //.atZone(ZoneId.of(eventtz))
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime()

                                            val eventenddatetime = Instant.ofEpochMilli(eventend)
                                                //.atZone(ZoneId.of(eventtzend))
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime()
                                            val game = Game(eventtitle, eventstartdatetime, eventenddatetime)
                                            gameslist.add(game)
                                        }
                                        gameslist.sortBy { it.start }
                                        Log.i(
                                            "LoadCalenderEvents", "${gameslist.size} Calender Event(s) added"
                                        )
                                    }
                                }

                            }




                        })
                }

            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
// https://stackoverflow.com/questions/76465858/how-to-access-state-from-another-composable-in-jetpack-compose
fun ShowCalenderEvents(gameslist:MutableList<Game>) {


            Text(
            text = "Game Events",
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn( modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f) ) {
            items(gameslist) { game ->
                Text(text = game.name + ":" + game.start, modifier = Modifier.padding(8.dp))
            }
        }

    }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreatePlayList(gameslist: List<Game>, jingleslist: MutableList<Jingle>, delayminutes:MutableState<String>) {
    Log.i("CreatePlayList","Create Jingle list with ${gameslist.size} game(s) and delay of ${delayminutes}" )
    jingleslist.clear()
    for (index in gameslist.indices) {
        val game: Game = gameslist[index]
        val jinglestart = Jingle(game.name, "Start", game.start)
        val jingleend = Jingle(game.name, "End", game.end)
        val enventdeltaenddatetime =
            game.end.minusMinutes(delayminutes.value.toLong())
        val jingledeltaend =
            Jingle(game.name, "DeltaEnd", enventdeltaenddatetime)
        jingleslist.add(jinglestart)
        jingleslist.add(jingleend)
        if (delayminutes.value.toLong() > 0) {
            jingleslist.add(jingledeltaend)
        }
    }
    // sort by Start of jingle
    jingleslist.sortBy { it.start}
    Log.i("CreatePlayList","${jingleslist.size} jingles added to Playlist")
    TextField(
        value = delayminutes.value,
        onValueChange = { newValue ->
            // Ensure the input is an integer
            if (newValue.toIntOrNull() != null || newValue.isEmpty()) {
                delayminutes.value = newValue
            }
        },
        label = { Text("Minutes before end to play jingle") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Text(
        text = "Playlist",
        modifier = Modifier.padding(16.dp)
    )
    LazyColumn( modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.3f)
         ) {
        items(jingleslist) { jingle ->
            Text(text = jingle.name + " " + jingle.type + " "+ jingle.start, modifier = Modifier.padding(8.dp))
        }

    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FindNextSong(jingleslist: List<Jingle>, nextsong: MutableState<Nextsong>) {
    Log.i("FindNextSong","Starting Scheduler with ${jingleslist.size} jingles" )

    val now = LocalDateTime.now()

    val firstPositiveDurationJingle = jingleslist
        .map { jingle ->
            val duration = Duration.between(now, jingle.start)
            jingle to duration
        }
        .firstOrNull { (_, duration) -> !duration.isNegative }
    var textit = ""
    if (firstPositiveDurationJingle != null) {
        val (nextjingle, nextjingleduration) = firstPositiveDurationJingle

        nextsong.value=Nextsong(nextjingle.name,nextjingle.type,nextjingleduration)
        textit = "${nextjingle.name}: Start in ${nextjingleduration.toMinutes()} min"
        Log.i("FindNextSong","First jingle with positive duration: ${nextjingle.name}, Time to start: ${nextjingleduration.toMinutes()} minutes")
    } else {
        Log.i("FindNextSong","No jingle with positive duration found.")
        textit = "No more jingles"
    }
    Text(
        text =textit ,
        modifier = Modifier.padding(16.dp)
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Countdown(nextsong: Nextsong){
    Log.i("countdown","Starting Countdown" )
    val context = LocalContext.current
    var isTimeReached =false

    LaunchedEffect(Unit) {
        while (nextsong.duration.toMillis()>5000){
            delay(1000)
        }
        isTimeReached=true
    }
    if(isTimeReached){PlaySound(context,nextsong)}
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlaySound(context:Context,nextsong: Nextsong) {
    // val context = LocalContext.current
    // which sounds should be played
    val soundMap = mutableMapOf(
        "Start" to R.raw.sound,
        "End" to R.raw.soundend,
        "DeltaEnd" to R.raw.sound5min
    )
    val songId = soundMap[nextsong.type]
    if (songId != null) {
        val mediaPlayer = remember { MediaPlayer.create(context, songId) }
        mediaPlayer.start()
        Log.i("PlaySound", "Playing sound: ${nextsong.name}")
    }
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.sound) }
    mediaPlayer.start()

    // Button to play sound
    /*
    IconButton(onClick = {mediaPlayer.start() }) {
        Icon(
            painter = painterResource(R.drawable.play_arrow_24px),
            contentDescription = "Play",
        )
    }
    IconButton(onClick = {mediaPlayer.pause() }) {
        Icon(
            painter = painterResource(R.drawable.pause_24px),
            contentDescription = "Pause",
        )
    }
    IconButton(onClick = {mediaPlayer.stop() }) {
        Icon(
            painter = painterResource(R.drawable.stop_24px),
            contentDescription = "Stop",
        )
    }

     */

    // Dispose of the MediaPlayer when the composable is removed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
}

