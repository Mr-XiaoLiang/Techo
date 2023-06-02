package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class SmsContentBuilderPage : ContentBuilder() {

    private var message by remember()
    private var phoneNumber by remember()

    override fun getContentValue(): String {
        val sms = BarcodeInfo.Sms()
        sms.message = message
        sms.phoneNumber = phoneNumber
        return sms.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.phone_number,
                InputConfig.PHONE,
                { phoneNumber },
            ) {
                phoneNumber = it.toString()
            }
            Space()
            Input(
                R.string.sms_message,
                InputConfig.CONTENT,
                { message },
            ) {
                message = it.toString()
            }
            SpaceEnd()
        }
    }
}