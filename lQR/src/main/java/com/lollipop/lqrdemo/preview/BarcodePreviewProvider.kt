package com.lollipop.lqrdemo.preview

import android.view.ViewGroup
import com.lollipop.pigment.Pigment
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper

class BarcodePreviewProvider {

    private val renderers = mutableMapOf<Class<out BarcodeInfo>, BarcodePreviewRenderer>()

    private var currentRenderer: BarcodePreviewRenderer? = null

    private fun getRenderer(barcode: BarcodeInfo): BarcodePreviewRenderer? {
        val clazz = barcode::class.java
        val renderer = renderers[clazz]
        if (renderer != null) {
            return renderer
        }
        val rendererSymbol = BarcodePreviewRendererMap.findRenderer(barcode)
        if (rendererSymbol != null) {
            val instance = rendererSymbol.getDeclaredConstructor().newInstance()
            renderers[clazz] = instance
            return instance
        }
        return null
    }

    fun hasPreview(barcode: BarcodeWrapper): Boolean {
        val renderer = getRenderer(barcode.info)
        return renderer != null
    }

    fun onDecorationChanged(pigment: Pigment) {
        currentRenderer?.onDecorationChanged(pigment)
    }

    fun showPreview(barcode: BarcodeWrapper, group: ViewGroup) {
        val renderer = getRenderer(barcode.info) ?: return
        val view = renderer.getView(group)
        group.removeAllViews()
        group.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        renderer.render(barcode)
        currentRenderer = renderer
    }

    fun clearPreview(group: ViewGroup) {
        currentRenderer = null
        group.removeAllViews()
    }

}