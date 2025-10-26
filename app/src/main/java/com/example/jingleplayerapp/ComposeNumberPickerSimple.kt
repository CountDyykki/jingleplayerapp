import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.jingleplayerapp.R

@Composable
fun SimpleNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: (Int) -> String = { it.toString() },
    description: String
) {
    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text=description)
        IconButton(
            onClick = {
                if (value > range.first) {
                    onValueChange(value - 1)
                }
            },
            enabled = value > range.first
        ) {

            Icon(
                painter = painterResource(R.drawable.arrow_drop_down_24px),
                contentDescription = "Decrement",
            )
        }


        Text(
            text = label(value),
            //modifier = Modifier.widthIn(min = 40.dp),
            // style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )

        IconButton(
            onClick = {
                if (value < range.last) {
                    onValueChange(value + 1)
                }
            },
            enabled = value < range.last
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_drop_up_24px),
                contentDescription = "Increment",
            )
        }
    }
}
