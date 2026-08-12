package com.example.neoradio.ui.screen.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SleepTimerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    var text by remember { mutableStateOf("10") }
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Bedtime,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Text("Χρονόμετρο", fontSize = 22.sp, fontWeight = FontWeight.W500)
                }
                OutlinedTextField(
                    value = text,
                    shape = CircleShape,
                    onValueChange = {
                        text = it
                    },
                    singleLine = true,
                    suffix = {
                        Text("λεπτά")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        }
                    ) {
                        Text("Ακύρωση")
                    }
                    Button(
                        onClick = {
                            onConfirm(text.toLongOrNull())
                            onDismissRequest()
                        }
                    ) {
                        Text("Ορισμός")
                    }
                }
            }
        }
    }
}