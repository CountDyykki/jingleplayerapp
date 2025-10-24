import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.jingleplayerapp.Songtoplay
import com.example.jingleplayerapp.Startplaying
import com.example.jingleplayerapp.Stopplaying
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

@OptIn(UnstableApi::class)
@Composable
fun Scheduler(exoPlayer:ExoPlayer, jingleslist: List<Jingle>, nextsong: MutableState<Songtoplay>, jinglelength: MutableState<Int>, textit:MutableState<String>) {
    var startPlaying = remember { mutableStateOf<Boolean>(false) }
    val lastsong = remember { mutableStateOf<Songtoplay>(nextsong.value) }
    var songtoplay = nextsong.value
    //LaunchedEffect(Unit) {
    //    while(true){
    if (jingleslist.isNotEmpty()) {
        val now = Instant.now()
        val nextjingle = jingleslist.first()
        val duration = Duration.between(now, nextjingle.start)
        nextsong.value = Songtoplay(nextjingle.name, nextjingle.type)
        if(!startPlaying.value) {
                textit.value =
                    "${nextjingle.name} | ${nextjingle.type}: Play in ${duration.toMinutes()} min."
        }
        Log.i(
            "Scheduler",
            "First jingle with positive duration: ${nextjingle.name}, Time to start: ${duration.toMinutes()} minutes"
        )
        // wenn die playlist einen weiterspringt
        Log.i("Scheduler", "name: ${lastsong.value.name}")
        if (lastsong.value != nextsong.value && lastsong.value.name!="") {
            // spiele den song davor einmal ab
            songtoplay=lastsong.value
            Log.i(
                "Scheduler",
                "Playing ${songtoplay.type}-jingle for ${songtoplay.name} "
            )
            textit.value = "${songtoplay.name} | ${songtoplay.type}: Playing"
            // firing launch effect
            startPlaying.value = true
        }

        lastsong.value = nextsong.value

    } else {
        Log.i("Scheduler", "No jingle with positive duration found.")
        textit.value = "No jingles left to play:\nSelect Calendar to Load"
    }
    LaunchedEffect(startPlaying.value) {
        Log.i("Launcheffect","playing ${songtoplay.name} and type ${songtoplay.type}")
        if (startPlaying.value) {
            Startplaying(exoPlayer, songtoplay)
            delay(jinglelength.value.toLong() * 1000)
            Stopplaying(exoPlayer)
            startPlaying.value = false
        }
    }
}

fun createjingles(
    gamesList: List<Game>,
    jingleslist: MutableList<Jingle>,
    delayMinutes: MutableState<Int>
) {
    jingleslist.clear()
    for (game in gamesList) {
        val now = Instant.now()
        val jingleStart = Jingle(game.name, "Start", game.start)
        val jingleEnd = Jingle(game.name, "End", game.end)
        val eventDeltaEndDateTime = game.end.minusSeconds(delayMinutes.value.toLong() * 60)
        val jingleDeltaEnd = Jingle(game.name, "DeltaEnd", eventDeltaEndDateTime)

        if (now.isBefore(jingleStart.start)) {
            jingleslist.add(jingleStart)
        }
        if (now.isBefore(jingleEnd.start)) {
            jingleslist.add(jingleEnd)
        }
        if (now.isBefore(jingleDeltaEnd.start) && delayMinutes.value > 0) {
            jingleslist.add(jingleDeltaEnd)
        }
    }
    jingleslist.sortBy { it.start }
    Log.i("CreatePlayList", "${jingleslist.size} jingles left in Playlist")
}
