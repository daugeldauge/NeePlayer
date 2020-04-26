package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.graphics.Color
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.Typography
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.sp

@Composable
fun NeeTheme(content: @Composable() () -> Unit) {
    MaterialTheme(colors = lightColorPalette(), typography = typography()) {
        Surface {
            content()
        }
    }
}

private fun typography(): Typography {
    val black87 = Color(0xFF212121)
    val black54 = Color(0xFF757575)
    return Typography(
            body1 = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    color = black87
            ),
            body2 = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    color = black54
            )
    )
}