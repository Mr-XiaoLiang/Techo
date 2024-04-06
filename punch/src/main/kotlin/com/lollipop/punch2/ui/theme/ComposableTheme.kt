package com.lollipop.punch2.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
fun TechoTheme(
    theme: State<ColorScheme?>? = null,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = theme?.value ?: MaterialTheme.colorScheme,
        typography = Typography,
        content = content
    )
}