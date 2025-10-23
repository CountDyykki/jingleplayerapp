// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp
import android.content.Context
import android.util.Log
import android.database.Cursor
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.runtime.setValue

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Duration



data class Jingle(val name: String, val type: String, val start: Instant)
data class Game(val name: String,  val start: Instant,val end: Instant)

data class Songtoplay(val name:String, val type:String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JingleplayerappTheme {
                Mainmenu(this)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable

fun Mainmenu(context: Context) {
    val delayminutes = remember { mutableIntStateOf(5) }
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
            val songtoplay = remember {mutableStateOf<Songtoplay>(Songtoplay("","Start"))}
            BottomAppBar(
                content = {
                    FindNextSong(context,jingleslist,songtoplay)
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

    val calnames =  remember {mutableListOf<String>("Select Calender")}
    val calmap =  remember {mutableMapOf<String, Long>()}





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
                            val displayname = getString(PROJECTION_DISPLAY_NAME_INDEX)
                            val id = getLong(PROJECTION_ID_INDEX)
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
                            val calId = calIdstate.value.toString()
                            val urievent = CalendarContract.Events.CONTENT_URI
                            val selectionevent = "${CalendarContract.Events.CALENDAR_ID}=${calId}"
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


                                            val eventenddatetime = Instant.ofEpochMilli(eventend)

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




@Composable

fun CreatePlayList(gameslist: List<Game>, playlist: MutableList<Jingle>, delayminutes:MutableState<Int>) {

    Log.i("CreatePlayList","Create Jingle list with ${gameslist.size} game(s) and delay of ${delayminutes}" )


     //   LaunchedEffect(Unit) {
       //     while (true) {
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
                    // delay(10000)

                }


     //       }
     //   }
    Box(modifier = Modifier.fillMaxSize(0.2f)) {
        NumberPicker(
            // state = remember { mutableStateOf(5) },
            state =  delayminutes ,
            range = 0..30,
            modifier = Modifier.align(Alignment.Center)
        )
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
fun FindNextSong(context:Context, playlist: List<Jingle>, nextsong: MutableState<Songtoplay>) {
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
                    "LaunchedEffect",
                    "First jingle with positive duration: ${nextjingle.name}, Time to start: ${duration.toMinutes()} minutes"
                )
                // wenn die playlist einen weiterspringt
                if (songtoplay.value != nextsong.value){
                // spiele den song davor einmal ab
                    PlaySound(context,songtoplay.value)
                songtoplay.value=nextsong.value
                }
            } else {
                Log.i("LaunchedEffect", "No jingle with positive duration found.")
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
fun PlaySound(context:Context,songtoplay: Songtoplay) {


    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.sound) }
    val soundMap = mutableMapOf(
        "Start" to R.raw.sound,
        "End" to R.raw.soundend,
        "DeltaEnd" to R.raw.sound5min
    )
    val soundId=soundMap[songtoplay.type]
    if (soundId!=null) {
        val mediaPlayer = remember { MediaPlayer.create(context, soundId) }
        mediaPlayer.start()
        Log.i("PlaySound", "Playing ${songtoplay.type}-jingle for ${songtoplay.name} ")
    }
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



