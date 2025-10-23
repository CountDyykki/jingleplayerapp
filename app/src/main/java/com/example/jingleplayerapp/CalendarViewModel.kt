import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.provider.CalendarContract
import android.content.Context
import android.database.Cursor
import android.util.Log

data class CalendarInfo(val id: Long, val displayName: String)

data class CalendarUiState(
    val calendars: List<CalendarInfo> = emptyList(),
    val selectedCalendar: CalendarInfo? = null,
    val isLoading: Boolean = false,
    val isDropdownExpanded: Boolean = false
)


class CalendarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun fetchCalendars(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendars = getCalendarsFromContentProvider(context)
            _uiState.update {
                it.copy(
                    calendars = calendars,
                    isLoading = false,
                    selectedCalendar = calendars.firstOrNull()
                )
            }
            // Load events for the initially selected calendar if it exists
            uiState.value.selectedCalendar?.let { loadEvents(context, it.id) }
        }
    }

    private fun getCalendarsFromContentProvider(context: Context): List<CalendarInfo> {
        val calendars = mutableListOf<CalendarInfo>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        )
        val uri = CalendarContract.Calendars.CONTENT_URI

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val displayName = cursor.getString(1)
                calendars.add(CalendarInfo(id, displayName))
            }
        }
        return calendars
    }

    fun loadEvents(context: Context, calendarId: Long) {
        // Implement logic to load events for the given calendar ID
        // This will be another coroutine function that updates a different StateFlow
        Log.i("CalendarViewModel", "Loading events for calendar ID: $calendarId")
    }

    fun selectCalendar(calendar: CalendarInfo, context: Context) {
        _uiState.update {
            it.copy(
                selectedCalendar = calendar,
                isDropdownExpanded = false
            )
        }
        loadEvents(context, calendar.id)
    }

    fun setDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isDropdownExpanded = expanded) }
    }
}