import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.provider.CalendarContract
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.time.Instant
import kotlin.collections.List

// Ein Kalendar ist eine id mit einer Kalenderbezeichnung
data class CalendarInfo(val id: Long, val displayName: String)
// ein Game ist ein Kalendereintrag, also eine Bezeichnung mit Start und Ende
data class Game(val name: String,  val start: Instant,val end: Instant)
// Ein Song ist ein Titel mit

data class CalendarUiState(
    val calendars: List<CalendarInfo> = emptyList(),
    val games: List<Game> = emptyList(),
    val selectedCalendar: CalendarInfo? = null,
    val isLoading: Boolean = false,
    val isDropdownExpanded: Boolean = false
)

class CalendarViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class CalendarViewModel(
    application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun fetchCalendars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendars = getCalendarsFromContentProvider()
            _uiState.update {
                it.copy(
                    calendars = calendars,
                    isLoading = false,
                    selectedCalendar = calendars.firstOrNull()
                )
            }
            // Load events for the initially selected calendar if it exists
            uiState.value.selectedCalendar?.let {loadEvents( it.id) }
        }
    }

    private fun getCalendarsFromContentProvider(): List<CalendarInfo> {
        val calendars = mutableListOf<CalendarInfo>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        )
        val uri = CalendarContract.Calendars.CONTENT_URI
        calendars.add(CalendarInfo(-1000, "TestCalender"))
        application.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val displayName = cursor.getString(1)
                calendars.add(CalendarInfo(id, displayName))
            }
        }
        return calendars
    }

    fun loadEvents(calendarId: Long): List<Game> {
        // Implement logic to load events for the given calendar ID
        // This will be another coroutine function that updates a different StateFlow

        Log.i("CalendarViewModel", "Loading events for calendar ID: $calendarId")
        val gameslist = mutableListOf<Game>()
        gameslist.clear()
            if (calendarId.toInt()==-1000) {
                val game=Game("Testspiel", Instant.ofEpochMilli(System.currentTimeMillis()+30000),Instant.ofEpochMilli(System.currentTimeMillis()+30000+6*60*1000))
                gameslist.add(game)
            }
        else{
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
            val calId = calendarId.toString()
            val urievent = CalendarContract.Events.CONTENT_URI
            val selectionevent = "${CalendarContract.Events.CALENDAR_ID}=${calId}"
            val eventscursor: Cursor? = application.contentResolver.query(
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
                    eventscursor.apply {
                        while (moveToNext()) {
                            val eventtitle = getString(EVENTPROJECTION_TITLE_INDEX)
                            val eventstart = getLong(EVENTPROJECTION_DTSTART_INDEX)
                            val eventend = getLong(EVENTPROJECTION_DTEND_INDEX)
                            //val eventtz = getString(EVENTPROJECTION_TZ_INDEX)
                            //val eventtzend = getString(EVENTPROJECTION_TZEND_INDEX)

                            val eventstartdatetime =
                                Instant.ofEpochMilli(eventstart)


                            val eventenddatetime = Instant.ofEpochMilli(eventend)

                            val game = Game(
                                eventtitle,
                                eventstartdatetime,
                                eventenddatetime
                            )
                            gameslist.add(game)
                        }
                    }

                }
            }
            }
        gameslist.sortBy { it.start }
        Log.i(
            "LoadCalenderEvents", "${gameslist.size} Calender Event(s) added"
        )
        return gameslist
        }


    fun selectCalendar(calendar: CalendarInfo) {
        var games: List<Game>
        games = loadEvents( calendar.id)
        _uiState.update {
            it.copy(
                selectedCalendar = calendar,
                games = games,
                isDropdownExpanded = false
            )
        }

    }


    fun setDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isDropdownExpanded = expanded) }
    }



}