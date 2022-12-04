package com.lollipop.qr.writer

enum class ForceCodeSet(override val zxing: String) : ZxingWrapper<String> {

    A("A"),
    B("B"),
    C("C")

}