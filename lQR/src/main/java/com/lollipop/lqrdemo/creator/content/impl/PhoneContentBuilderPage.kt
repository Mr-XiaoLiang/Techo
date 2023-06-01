package com.lollipop.lqrdemo.creator.content.impl

import android.text.InputType
import com.lollipop.base.util.dp2px
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
            Space(16.dp2px)
            Input(
                context.getString(R.string.phone_number),
                InputConfig(
                    inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_VARIATION_NORMAL),
                ),
                { number },
            ) {
                number = it.toString()
            }
            Space(26.dp2px)
        }
    }
}