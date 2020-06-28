package com.neeplayer.compose

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.graphics.Color
import androidx.ui.layout.size
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.Typography
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import androidx.ui.unit.sp

object NeeColors {
    val imageAlt = Color(0xffe0e0e0)
    val black87 = Color(0xff212121)
    val black54 = Color(0xff757575)
}

@Composable
fun NeeTheme(content: @Composable() () -> Unit) {
    MaterialTheme(colors = lightColorPalette(), typography = typography()) {
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