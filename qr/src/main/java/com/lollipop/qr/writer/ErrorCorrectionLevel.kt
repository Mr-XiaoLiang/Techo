package com.lollipop.qr.writer

enum class ErrorCorrectionLevel(
    override val zxing: com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
) : ZxingWrapper<com.google.zxing.qrcode.decoder.ErrorCorrectionLevel> {

    L(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L),

    /** M = ~15% correction  */
    M(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M),

    /** Q = ~25% correction  */
    Q(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q),

    /** H = ~30% correction  */
    H(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H)

}