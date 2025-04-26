package com.lollipop.lqrdemo.preview

import com.lollipop.qr.comm.BarcodeInfo

object BarcodePreviewRendererMap {

    private val rendererMap = arrayOf<RendererSymbol>(
        // TODO("需要填充实现类")
    )

    fun findRenderer(barcode: BarcodeInfo): Class<out BarcodePreviewRenderer>? {
        for (symbol in rendererMap) {
            if (symbol.barcode == barcode.javaClass) {
                return symbol.renderer
            }
        }
        return null
    }

    class RendererSymbol(
        val barcode: Class<out BarcodeInfo>,
        val renderer: Class<out BarcodePreviewRenderer>
    )

}