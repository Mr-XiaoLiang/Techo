package com.lollipop.qr.writer

enum class Pdf417Compaction(
    override val zxing: com.google.zxing.pdf417.encoder.Compaction
) : ZxingWrapper<com.google.zxing.pdf417.encoder.Compaction> {

    AUTO(com.google.zxing.pdf417.encoder.Compaction.AUTO),
    TEXT(com.google.zxing.pdf417.encoder.Compaction.TEXT),
    BYTE(com.google.zxing.pdf417.encoder.Compaction.BYTE),
    NUMERIC(com.google.zxing.pdf417.encoder.Compaction.NUMERIC)

}