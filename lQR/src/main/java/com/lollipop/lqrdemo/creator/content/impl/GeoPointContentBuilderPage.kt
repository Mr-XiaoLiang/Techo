package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class GeoPointContentBuilderPage : ContentBuilder() {

    private var lat by remember { "0.0" }
    private var lng by remember { "0.0" }

    override fun getContentValue(): String {
        val geoPoint = BarcodeInfo.GeoPoint()
        geoPoint.lat = getDouble(lat)
        geoPoint.lng = getDouble(lng)
        return geoPoint.getBarcodeValue()
    }

    private fun getDouble(value: String): Double {
        try {
            if (value.isEmpty()) {
                return 0.0
            }
            return value.toDouble()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return 0.0
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.geo_lat,
                InputConfig.NUMBER,
                { lat },
            ) {
                lat = it
            }
            Input(
                R.string.geo_lng,
                InputConfig.NUMBER,
                { lng },
            ) {
                lng = it
            }
            SpaceEnd()
        }
    }
}