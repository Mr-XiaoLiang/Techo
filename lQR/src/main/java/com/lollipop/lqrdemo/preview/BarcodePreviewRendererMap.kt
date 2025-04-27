package com.lollipop.lqrdemo.preview

import com.lollipop.lqrdemo.preview.renderer.WebViewRenderer
import com.lollipop.qr.comm.BarcodeInfo

object BarcodePreviewRendererMap {

    // TODO("需要填充实现类")
    private val rendererMap = arrayOf<RendererSymbol>(
        RendererSymbol(BarcodeInfo.Url::class.java, WebViewRenderer::class.java),
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