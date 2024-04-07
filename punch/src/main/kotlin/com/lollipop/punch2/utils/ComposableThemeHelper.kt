package com.lollipop.punch2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun ThemeHelper.liveTheme() = themeLive.observeAsState()
