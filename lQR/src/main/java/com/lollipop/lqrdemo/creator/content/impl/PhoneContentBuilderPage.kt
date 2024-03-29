package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class PhoneContentBuilderPage : ContentBuilder() {

    private var number by remember()

    override fun getContentValue(): String {
        val phone = BarcodeInfo.Phone()
        phone.number = number
        return phone.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.phone_number,
                InputConfig.PHONE,
                { number },
            ) {
                number = it
            }
            SpaceEnd()
        }
    }
}