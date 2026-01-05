import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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

@Composable
fun SelectJinglesScreen(jingleUriState: MutableState<JingleUriState>) {
    // val context = LocalContext.current
    Column(
        //modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Select Jingles", fontWeight = FontWeight.Bold)
        AudioPickerRow(jingleUriState,"Start")
        AudioPickerRow(jingleUriState,"PreEnd")
        AudioPickerRow(jingleUriState,"End")
    }
}

@Composable
fun AudioPickerRow(jingleUriState: MutableState<JingleUriState>,type:String) {
    val audioPickerLauncher = pickaudiofile(jingleUriState, type)
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp),
    ) {
        OutlinedButton(
            modifier = Modifier
                .width(100.dp)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            onClick = {
                audioPickerLauncher.launch("audio/*")
            }
        ) {
            Text(
                text =type,
                textAlign = TextAlign.Start, // Aligns the text to the left
                fontWeight = FontWeight.Bold // Optional: Make the text bold
            )
        }
        when (type) {
            "Start" -> {
                Text(jingleUriState.value.audioStart.name)
            }
            "PreEnd" -> {
                Text(jingleUriState.value.audioPreEnd.name)
            }
            "End" -> {
                Text(jingleUriState.value.audioEnd.name)
            }
            else -> {
                // Handle other cases or do nothing
            }
        }
    }
}

@Composable
fun pickaudiofile(jingleUriState: MutableState<JingleUriState>, type: String): ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val filename = getFileName(context, uri) ?: "Take Default"
            when (type) {
                "Start" -> {
                    jingleUriState.value = jingleUriState.value.copy(audioStart = JingleUri(uri, filename))
                }
                "PreEnd" -> {
                    jingleUriState.value = jingleUriState.value.copy(audioPreEnd = JingleUri(uri, filename))
                }
                "End" -> {
                    jingleUriState.value = jingleUriState.value.copy(audioEnd = JingleUri(uri, filename))
                }
                else -> {
                    // Handle other cases or do nothing
                }
            }
            Log.i("AudioPicker", "Selected audio URI: $uri")
        }
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
