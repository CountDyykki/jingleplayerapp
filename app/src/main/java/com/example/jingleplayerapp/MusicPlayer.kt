package com.example.jingleplayerapp

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.media3.common.MediaItem
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer


fun Startplaying( exoPlayer: ExoPlayer, songtoplay: Songtoplay){
    Log.i("PlaySong","PlaySong started for ${songtoplay.name} type ${songtoplay.type}")
    val soundMap = mutableMapOf(
        "Start" to R.raw.start,
        "End" to R.raw.end,
        "DeltaEnd" to R.raw.beforeend
    )
    val soundId=soundMap[songtoplay.type]
    Log.i("PlaySong","PlaySong started with ${soundId} condition ${soundId!=null}")
    if (soundId!=null) {
        Log.i("PlaySong","Playing song ${soundId}")
        val rawResourceUri = RawResourceDataSource.buildRawResourceUri(soundId)
        val mediaItem = MediaItem.fromUri(rawResourceUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        Log.i("PlaySong","Starting Exoplayer")
        exoPlayer.play()
    }
}


fun Stopplaying(exoPlayer:ExoPlayer){
    Log.i("PlaySong","Stopping Exoplayer")
    exoPlayer.stop()
}
@Composable
fun PlaySong( exoPlayer: ExoPlayer, startPlaying: MutableState<Boolean>, songtoplay: MutableState<Songtoplay>,jinglelength: MutableState<Int>){
    Log.i("PlaySong","PlaySong started for ${songtoplay.value.name} type ${songtoplay.value.type}")
    val soundMap = mutableMapOf(
        "Start" to R.raw.start,
        "End" to R.raw.end,
        "DeltaEnd" to R.raw.beforeend
    )
    val soundId=soundMap[songtoplay.value.type]
    Log.i("PlaySong","PlaySong started with ${soundId} condition ${soundId!=null}")
    if (soundId!=null) {
            Log.i("PlaySong","${startPlaying}")

            Log.i("PlaySong","Playing song ${soundId} for ${jinglelength.value} sec.")
            val rawResourceUri = RawResourceDataSource.buildRawResourceUri(soundId)
            val mediaItem = MediaItem.fromUri(rawResourceUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            Log.i("PlaySong","Starting Exoplayer")
            exoPlayer.play()

            Log.i("PlaySong","Stopping Exoplayer")
            exoPlayer.stop()

        }

}