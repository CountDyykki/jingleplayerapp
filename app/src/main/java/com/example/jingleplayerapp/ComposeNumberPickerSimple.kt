import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jingleplayerapp.R
import com.example.jingleplayerapp.UIstate


@Composable
fun Configuretimes(uistate: MutableState<UIstate>) {

    // are copied over to the global uistate class
    var delayminutes = remember { mutableIntStateOf(5) }
    var jinglelength = remember { mutableIntStateOf(10) }
    Log.i("Configure Times", "delayminutes: ${delayminutes} linglelength: ${jinglelength}")
    uistate.value = uistate.value.copy(
        minutesbeforeendgame = delayminutes.value,
        jinglelength = jinglelength.value
    )
    Text(text="Timing",fontWeight= FontWeight.Bold)
    Row{
        Column(){
            Text( "PreEnd Start time (min)")
            Text( "Jingle duration (sec)")
        }
        Column(){
            SimpleNumberPicker(
                label = { it.toString() },
                range = 0..30,
                onValueChange = { delayminutes.value = it },
                value = delayminutes.value
            )
            SimpleNumberPicker(
                label = { it.toString() },
                range = 0..30,
                onValueChange = { jinglelength.value = it },
                value = jinglelength.value
            )
        }
    }
}


@Composable
fun SimpleNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: (Int) -> String = { it.toString() }
) {
    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (value > range.first) {
                    onValueChange(value - 1)
                }
            },
            enabled = value > range.first,
            modifier = Modifier.size(24.dp) // Adjust the size of the IconButton
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_drop_down_24px),
                contentDescription = "Decrement",
                modifier = Modifier.size(24.dp),
                tint = LocalContentColor.current// Adjust the size of the Icon
            )
        }

        Text(
            text = label(value),
            modifier = Modifier.widthIn(min = 20.dp),
            maxLines = 1,
            textAlign = TextAlign.Center
           // fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = {
                if (value < range.last) {
                    onValueChange(value + 1)
                }
            },
            enabled = value < range.last,
            modifier = Modifier.size(24.dp) // Adjust the size of the IconButton
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_drop_up_24px),
                contentDescription = "Increment",
                modifier = Modifier.size(24.dp), // Adjust the size of the Icon
                // tint = LocalContentColor.current
            )
        }
    }
}

@Composable
fun SpinnerNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(range.toList()) { number ->
            TextButton(
                onClick = { onValueChange(number) },
                enabled = number != value
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}