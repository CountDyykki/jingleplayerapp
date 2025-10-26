import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class PlaybackService (context: Context){
    private var exoPlayer: ExoPlayer? = null



    init {
        val audioAttributes = AudioAttributes.Builder()
            // macht dann komplett laut und die musik auf pause
             .setUsage(C.USAGE_ALARM)
            // spielt glaube ich beides gleichzeitig ab
            // .setUsage(C.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer?.setAudioAttributes(audioAttributes, false)
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    Log.i("Schedule player", "Playback ended. Releasing player.")
                    exoPlayer?.release()
                    exoPlayer = null
                }
            }
        }
        exoPlayer?.addListener(listener)


    }

    fun playjingle(uri:Uri){

        exoPlayer?.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    fun stopplaying(){
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }










}