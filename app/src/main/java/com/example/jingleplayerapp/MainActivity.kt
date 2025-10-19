// https://www.geeksforgeeks.org/android/how-to-build-a-simple-music-player-app-using-android-kotlin/

package com.example.jingleplayerapp

import android.database.Cursor
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.provider.CalendarContract
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    // Declare MediaPlayer for audio playback
    private lateinit var mediaPlayer: MediaPlayer

    // Declare UI elements
    private lateinit var seekBar: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var textTotalTime: TextView
    private lateinit var buttonPlay: ImageView
    private lateinit var buttonPause: ImageView
    private lateinit var buttonStop: ImageView

    private lateinit var buttoncalselect: ImageView


    companion object {
        private val CALENDAR_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.SYNC_EVENTS,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
        )
        private const val PROJECTION_ID_INDEX = 0
        private const val PROJECTION_DISPLAY_NAME_INDEX = 1
        private const val PROJECTION_NAME_INDEX = 2
        private const val PROJECTION_CALENDAR_COLOR_INDEX = 3
        private const val PROJECTION_VISIBLE_INDEX = 4
        private const val PROJECTION_SYNC_EVENTS_INDEX = 5
        private const val PROJECTION_ACCOUNT_NAME_INDEX = 6
        private const val PROJECTION_ACCOUNT_TYPE_INDEX = 7
    }


    // Handler to update SeekBar and current time text every second
    private val handler = Handler(Looper.getMainLooper())

    // Runnable task that updates SeekBar and current playback time
    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {

                // Update SeekBar progress and current time text
                seekBar.progress = mediaPlayer.currentPosition
                textCurrentTime.text = formatTime(mediaPlayer.currentPosition)

                // Repeat this task every 1 second
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views from layout
        seekBar = findViewById(R.id.seekBar)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        textTotalTime = findViewById(R.id.textTotalTime)
        buttonPlay = findViewById(R.id.buttonPlay)
        buttonPause = findViewById(R.id.buttonPause)
        buttonStop = findViewById(R.id.buttonStop)
        buttoncalselect=findViewById(R.id.buttoncalselect)
        val calspinner: Spinner = findViewById(R.id.calspinner)


        // Read calender items
       /*

        val uri = CalendarContract.Calendars.CONTENT_URI
        val selection = ""
        val selectionArgs = emptyArray<String>()
        val cursor = contentResolver.query(
            uri,
            CALENDAR_PROJECTION,
            selection, selectionArgs,
            null,
        )

        val adapter = SimpleCursorAdapter(
            this,
           R.layout.calspinner,
            cursor,
            arrayOf(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME),
            intArrayOf(R.id.textView)
        )
        // adapter.setDropDownViewResource(R.layout.)
        calspinner.adapter=adapter



        buttoncalselect.setOnClickListener{
            // id vom gewählten kalender einlesen
            val EVENT_PROJECTION: Array<String> = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )
            val PROJECTION_ID_INDEX: Int = 0
            val PROJECTION_TITLE_INDEX: Int = 1
            val PROJECTION_DTSTART_INDEX: Int = 2
            val PROJECTION_DTEND_INDEX: Int = 3

            val spinnerentry = calspinner.getSelectedItem().toString()
            while (cursor?.moveToNext() == true) {
                val displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX)
                if (displayName == spinnerentry) {
                    val calId = cursor.getLong(PROJECTION_ID_INDEX)
                    break
                }
            }
            val uri = CalendarContract.Events.CONTENT_URI
            val selection = ""
            val selectionArgs = emptyArray<String>()
            val cursorevents = contentResolver.query(
                uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                null,
            )
        }

 */


        // Create MediaPlayer instance with a raw audio resource
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)

        // on below line we are adding set on
        // date change listener for calendar view.


        // Set listener to configure SeekBar and total time after MediaPlayer is ready
        mediaPlayer.setOnPreparedListener {
            seekBar.max = it.duration
            textTotalTime.text = formatTime(it.duration)
        }

        // Play button starts the audio and begins updating UI
        buttonPlay.setOnClickListener {
            mediaPlayer.start()
            handler.post(updateSeekBar)
        }

        // Pause button pauses the audio playback
        buttonPause.setOnClickListener {
            mediaPlayer.pause()
        }

        // Stop button stops playback and resets UI and MediaPlayer
        buttonStop.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer = MediaPlayer.create(this, R.raw.sound)
            seekBar.progress = 0
            textCurrentTime.text = "0:00"
            textTotalTime.text = formatTime(mediaPlayer.duration)
        }

        // Listen for SeekBar user interaction
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            // Called when progress is changed
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Seek MediaPlayer to new position and update current time
                    mediaPlayer.seekTo(progress)
                    textCurrentTime.text = formatTime(progress)
                }
            }

            // Not used, but required to override
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            // Not used, but required to override
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }


    // Format milliseconds into minutes:seconds format (e.g., 1:05)
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%d:%02d", minutes, seconds)
    }



    // Clean up MediaPlayer and handler when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}

