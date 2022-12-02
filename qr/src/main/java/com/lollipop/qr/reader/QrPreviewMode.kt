package com.lollipop.qr.reader

import androidx.camera.view.PreviewView

enum class QrPreviewMode(val mode: PreviewView.ImplementationMode) {
    PERFORMANCE(PreviewView.ImplementationMode.PERFORMANCE),
    COMPATIBLE(PreviewView.ImplementationMode.COMPATIBLE),
}