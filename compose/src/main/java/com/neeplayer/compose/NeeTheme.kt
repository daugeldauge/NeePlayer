package com.neeplayer.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object NeeColors {
    val imageAlt = Color(0xffe0e0e0)
    val black87 = Color(0xff212121)
    val black54 = Color(0xff757575)
}

@Composable
fun NeeTheme(content: @Composable() () -> Unit) {
    MaterialTheme(colors = lightColors(), typography = typography()) {
        Surface {
            content()
        }
    }
}

private fun typography(): Typography {
    return Typography(
        body1 = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            color = NeeColors.black87
        ),
        body2 = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            letterSpacing = 0.sp,
            color = NeeColors.black54
        )
    )
}