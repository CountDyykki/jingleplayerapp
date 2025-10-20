// icons from https://fonts.google.com/icons?selected=Material+Symbols+Outlined:stop:FILL@0;wght@400;GRAD@0;opsz@24&icon.size=24&icon.color=%231f1f1f&icon.platform=android
package com.example.jingleplayerapp
import android.util.Log
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// a single calendar event as a class
data class Game(val name: String, val start: LocalDateTime, val end: LocalDateTime)
data class Jingle(val game: String, val type: String, val start: LocalDateTime)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JingleplayerappTheme {
                Mainmenu()
            }
        }
    }
}


@Composable
fun LoadCalendarButton() {
    TextButton(onClick = { }) {
        Text("Load Calendar")
    }
}


@Composable
fun Mainmenu() {

    Scaffold(
        topBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                content =
                    {
                        LoadCalendarButton()
                        Loadevents()
                        //val calNamestate = remember { mutableStateOf("") }
                        //val calIdstate = remember { mutableLongStateOf(0) }
                        // DropDownDemo(calNamestate=calNamestate,calIdstate=calIdstate)
                        IconButton(onClick = { }) {
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = "Play",
                            )
                        }
                    })
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.play_arrow_24px),
                            contentDescription = "Play",
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.pause_24px),
                            contentDescription = "Pause",
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.stop_24px),
                            contentDescription = "Stop",
                        )
                    }

                })

        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(
                    painter = painterResource(R.drawable.play_arrow_24px),
                    contentDescription = "toll",
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Ein schoener Text",
            )
        }
    }
}
























@RequiresApi(Build.VERSION_CODES.O)
@Composable
// https://stackoverflow.com/questions/76465858/how-to-access-state-from-another-composable-in-jetpack-compose
fun Loadevents(){
    Log.i("CalenderEvents","reading events")
    val context = LocalContext.current
    val calNamestate = remember { mutableStateOf("") }
    val calIdstate = remember { mutableLongStateOf(-1) }
    DropDownDemo(calNamestate=calNamestate,calIdstate=calIdstate)
    // define a query for the calendar
    val PROJECTION = arrayOf(
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
    )
    val PROJECTION_CALENDARID_INDEX = 0
    val PROJECTION_TITLE_INDEX = 1
    val PROJECTION_DTSTART_INDEX = 2
    val PROJECTION_DTEND_INDEX = 3
    var calId = calIdstate.value.toString()
    Log.i("CalenderEvents","Calidstate is ${calId}")


    val uri = CalendarContract.Events.CONTENT_URI
    val selection: String = "${CalendarContract.Events.CALENDAR_ID}=${calId}"
    // val selectionArgs :Array<String> = arrayOf(calId)
    // run the query for the calendar events
    //val selectionArgs = emptyArray<String>()
    //val selection:String=""




        val eventscursor: Cursor? = context.contentResolver.query(
            uri,
            PROJECTION,
            selection,
            null,
            null
        )
        Log.i("CalenderEvents","läuft noch")

        when (eventscursor?.count) {
            null -> {
                Log.e("CalenderEvents", "empty cursor")
            }

            0 -> {
                Log.i("CalenderEvents", "0 cursor")
            }

            else -> {
                Log.i("CalenderEvents", "working cursor")
                eventscursor.apply{
                    // a map of the events
                    var gamesmap=mutableListOf<Game>()
                    var jinglesmap=mutableListOf<Jingle>()
                    while (moveToNext()) {
                        val eventtitle = getString(PROJECTION_TITLE_INDEX)
                        val eventstart = getString(PROJECTION_DTSTART_INDEX).toLong()
                        val eventend = getString(PROJECTION_DTEND_INDEX).toLong()
                        val eventstartdatetime=Instant.ofEpochSecond(eventstart)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                        val eventenddatetime=Instant.ofEpochSecond(eventend)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                        val enventdeltaenddatetime=eventenddatetime.minusMinutes(5)

                        val game = Game(eventtitle, eventstartdatetime,eventenddatetime)
                        gamesmap.add(game)
                        // data class playEvent(val game: String, val type: String, val start: LocalDateTime, val musicfile:String)
                        val jinglestart=Jingle(eventtitle,"Start",eventstartdatetime)
                        val jingleend=Jingle(eventtitle,"End",eventenddatetime)
                        val jingledeltaend=Jingle(eventtitle,"DeltaEnd",enventdeltaenddatetime)
                        jinglesmap.add(jinglestart)
                        jinglesmap.add(jingleend)
                        jinglesmap.add(jingledeltaend)
                        Log.i("CalenderEvents", "Now adding ${eventtitle} starting at ${eventstartdatetime}")
                    }}
            }







    }



    }




@Composable
fun DropDownDemo(calNamestate: MutableState<String>,calIdstate: MutableState<Long>) {

    Log.i("DropDownMenu","filling drop down menu")
    // Initialize the list to store calendar names


    val isDropDownExpanded = remember {
        mutableStateOf(false)
    }

    val itemPosition = remember {
        mutableStateOf(0)
    }


    val context = LocalContext.current
    // Read all the calenders from the calendar app
    val CALENDAR_PROJECTION = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.CALENDAR_COLOR,
    )
    val PROJECTION_ID_INDEX = 0
    val PROJECTION_DISPLAY_NAME_INDEX = 1
    val PROJECTION_NAME_INDEX = 2
    val PROJECTION_CALENDAR_COLOR_INDEX = 3
    val uri = CalendarContract.Calendars.CONTENT_URI
    val selection = ""
    val selectionArgs = emptyArray<String>()
    val calendarscursor: Cursor? = context.contentResolver.query(
        uri,
        CALENDAR_PROJECTION,
        selection, selectionArgs,
        null,
    )

    var calnames =   mutableListOf<String>()
    var calmap = mutableMapOf<String,Long>()
    calendarscursor?.apply {
        while (moveToNext()) {
            var displayname=getString(PROJECTION_DISPLAY_NAME_INDEX)
            var id = getLong(PROJECTION_ID_INDEX)
            calnames.add(displayname)
            calmap.put(displayname,id)
        }
    }




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
                    isDropDownExpanded.value = true
                }
            ) {
                Text(text = calnames[itemPosition.value])
                Image(
                    painter = painterResource(id = R.drawable.more_vert_24px),
                    contentDescription = "DropDown Icon"
                )
            }
            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }) {



                calnames.forEachIndexed { index, calname ->
                    DropdownMenuItem(
                        text = {
                        Text(text = calname)
                        },
                        onClick = {
                            isDropDownExpanded.value = false
                            itemPosition.value = index
                            calNamestate.value=calname
                            Log.i("DropDownMenu","Setting calNamestate to $calname")
                            // get Calendar ID from map
                            calIdstate.value= calmap.getValue(calname)
                            Log.i("DropDownMenu","calidstate is ${calIdstate.value}")
                        })
                }

            }
        }

    }
}

