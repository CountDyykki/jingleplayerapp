import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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
        Button(
            modifier = Modifier.width(100.dp),
            onClick = {
                pickAudioLauncher.launch("audio/*")
            }
        ) {
            Text(text = buttontxt)
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
