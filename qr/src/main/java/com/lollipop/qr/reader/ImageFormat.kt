package com.lollipop.qr.reader

import com.google.mlkit.vision.common.InputImage

enum class ImageFormat(val code: Int, val mediaCode: Int) {

    NV21(InputImage.IMAGE_FORMAT_NV21, android.graphics.ImageFormat.NV21),
    YV12(InputImage.IMAGE_FORMAT_YV12, android.graphics.ImageFormat.YV12),
    BITMAP(InputImage.IMAGE_FORMAT_BITMAP, android.graphics.ImageFormat.UNKNOWN),
    YUV_420_888(InputImage.IMAGE_FORMAT_YUV_420_888, android.graphics.ImageFormat.YUV_420_888)

}