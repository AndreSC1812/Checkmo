package com.ascdev.checkmo.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CheckmoThemePreview() {
    CheckmoTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "08:42:15", style = MaterialTheme.typography.displayMedium)
                Card {
                    Text(
                        text = "Jornada de hoy",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = CheckmoTheme.extendedColors
                    StateButton("Entrada", colors.clockIn, colors.onClockIn)
                    StateButton("Pausa", colors.pause, colors.onPause)
                    StateButton("Salida", colors.clockOut, colors.onClockOut)
                }
                Button(onClick = {}) { Text("Acción principal") }
            }
        }
    }
}

@Composable
private fun StateButton(text: String, container: Color, content: Color) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    ) {
        Text(text)
    }
}
