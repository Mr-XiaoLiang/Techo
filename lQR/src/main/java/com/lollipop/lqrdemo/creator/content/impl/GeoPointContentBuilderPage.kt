package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class GeoPointContentBuilderPage : ContentBuilder() {

    private var lat: Double = 0.0
    private var lng: Double = 0.0

    override fun getContentValue(): String {
        val geoPoint = BarcodeInfo.GeoPoint()
//        TODO("Not yet implemented")
        return geoPoint.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
//        TODO("Not yet implemented")
    }
}