package com.lollipop.qr.reader

import androidx.camera.view.PreviewView

enum class QrPreviewScaleType(val type: PreviewView.ScaleType) {
    /**
     * 剪裁保留开始位置
     */
    FILL_START(PreviewView.ScaleType.FILL_START),

    /**
     * 剪裁保留中间位置
     */
    FILL_CENTER(PreviewView.ScaleType.FILL_CENTER),

    /**
     * 剪裁保留结束位置
     */
    FILL_END(PreviewView.ScaleType.FILL_END),

    /**
     * 缩放居上
     */
    FIT_START(PreviewView.ScaleType.FIT_START),

    /**
     * 缩放居中
     */
    FIT_CENTER(PreviewView.ScaleType.FIT_CENTER),

    /**
     * 缩放居尾
     */
    FIT_END(PreviewView.ScaleType.FIT_END);
}