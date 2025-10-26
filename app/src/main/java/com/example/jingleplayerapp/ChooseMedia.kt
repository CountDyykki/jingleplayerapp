import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jingleplayerapp.JingleUri
import com.example.jingleplayerapp.JingleUriState
import com.example.jingleplayerapp.UIstate




@Composable
fun SelectJinglesScreen(jingleUriState: MutableState<JingleUriState>) {
    val context = LocalContext.current
    val heightbox=50.dp
    val weight=0.2f
    Column(
        //modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
           // modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(heightbox)
                    .padding(0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AudioFilePickerScreen("Start", onAudioPicked = { uri ->
                    if (uri != null) {
                        val filename = getFileName(context, uri) ?: "Take Default"
                        jingleUriState.value = jingleUriState.value.copy(audioStart = JingleUri(uri, filename))
                    }
                })
            }
            Box(
                modifier = Modifier
                 //   .weight(weight)
                    .height(heightbox)
                    .padding(0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(jingleUriState.value.audioStart.name)
            }
        }

        Row(
          //  modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(heightbox)
                    .padding(0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AudioFilePickerScreen("PreEnd", onAudioPicked = { uri ->
                    if (uri != null) {
                        val filename = getFileName(context, uri) ?: "Take Default"
                        jingleUriState.value = jingleUriState.value.copy(audioPreEnd = JingleUri(uri, filename))
                    }
                })
            }
            Box(
                modifier = Modifier
                  //  .weight(weight)
                    .height(heightbox)
                    .padding(0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(jingleUriState.value.audioPreEnd.name)
            }
        }

        Row(
        //    modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(heightbox)
                    .padding(0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AudioFilePickerScreen("End", onAudioPicked = { uri ->
                    if (uri != null) {
                        val filename = getFileName(context, uri) ?: "Take Default"
                        jingleUriState.value = jingleUriState.value.copy(audioEnd = JingleUri(uri, filename))
                    }
                })
            }
            Box(
                modifier = Modifier
                 //   .weight(weight)
                    .height(heightbox)
                    .padding(0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(jingleUriState.value.audioEnd.name)
            }
        }
    }
}




@Composable
fun AudioFilePickerScreen(buttontxt: String, onAudioPicked: (Uri?) -> Unit) {
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Use the callback to notify the parent of the new URI
        if (uri != null) {
            onAudioPicked(uri)
            Log.i("AudioPicker", "Selected audio URI: $uri")
        }
    }
    OutlinedButton(
            modifier = Modifier
               // .width(70.dp)
                .padding(horizontal = 0.dp, vertical = 0.dp) // Reduce padding for a more compact size
                .wrapContentSize(),
                    onClick = {
                pickAudioLauncher.launch("audio/*")
            }
        ) {
            Text( text = buttontxt,
                textAlign = TextAlign.Start, // Aligns the text to the left
                fontWeight = FontWeight.Bold )// Optional: Make the text bold)
        }

}
fun getFileName(context: Context, uri: Uri?): String? {
    if (uri == null) return null
    var fileName: String? = null
    val contentResolver: ContentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}
