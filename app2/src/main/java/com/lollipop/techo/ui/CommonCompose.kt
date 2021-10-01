package com.lollipop.techo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState

/**
 * @author lollipop
 * @date 2021/9/29 21:52
 * 通用的Compose
 */

@Composable
fun loadImage(
    url: String,
    loadState: ((ImageLoadState) -> Unit) = {}
): Painter {
    val printer = rememberCoilPainter(request = url)
    loadState(printer.loadState)
    return printer
}