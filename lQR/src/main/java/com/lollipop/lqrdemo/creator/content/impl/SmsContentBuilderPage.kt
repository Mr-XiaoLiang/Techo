package com.lollipop.lqrdemo.creator.content.impl

import android.text.InputType
import com.lollipop.base.util.dp2px
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
            Space(16.dp2px)
            Input(
                context.getString(R.string.phone_number),
                InputConfig(
                    inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_VARIATION_NORMAL),
                ),
                { phoneNumber },
            ) {
                phoneNumber = it.toString()
            }
            Space(16.dp2px)
            Input(
                context.getString(R.string.sms_message),
                InputConfig(
                    inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
                        .or(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                ),
                { message },
            ) {
                message = it.toString()
            }
            Space(26.dp2px)
        }
    }
}