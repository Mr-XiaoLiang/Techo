package com.lollipop.qr

import com.google.mlkit.vision.common.InputImage

enum class ImageFormat(val code: Int) {

    NV21(InputImage.IMAGE_FORMAT_NV21),
    YV12(InputImage.IMAGE_FORMAT_YV12),
    BITMAP(InputImage.IMAGE_FORMAT_BITMAP),
    YUV_420_888(InputImage.IMAGE_FORMAT_YUV_420_888)

}