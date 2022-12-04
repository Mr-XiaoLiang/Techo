package com.lollipop.qr.writer


enum class SymbolShapeHint(
    override val zxing: com.google.zxing.datamatrix.encoder.SymbolShapeHint
) : ZxingWrapper<com.google.zxing.datamatrix.encoder.SymbolShapeHint> {

    FORCE_NONE(com.google.zxing.datamatrix.encoder.SymbolShapeHint.FORCE_NONE),
    FORCE_SQUARE(com.google.zxing.datamatrix.encoder.SymbolShapeHint.FORCE_SQUARE),
    FORCE_RECTANGLE(com.google.zxing.datamatrix.encoder.SymbolShapeHint.FORCE_RECTANGLE),

}