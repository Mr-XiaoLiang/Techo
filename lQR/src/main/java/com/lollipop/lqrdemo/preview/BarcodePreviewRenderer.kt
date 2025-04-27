package com.lollipop.lqrdemo.preview

import android.view.View
import android.view.ViewGroup
import com.lollipop.pigment.Pigment
import com.lollipop.qr.comm.BarcodeWrapper

interface BarcodePreviewRenderer {

    fun getView(container: ViewGroup): View

    fun render(barcode: BarcodeWrapper)

    fun onDecorationChanged(pigment: Pigment)

}