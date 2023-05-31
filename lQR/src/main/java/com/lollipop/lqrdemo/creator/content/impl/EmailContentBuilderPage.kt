package com.lollipop.lqrdemo.creator.content.impl

import android.text.InputType
import com.lollipop.base.util.dp2px
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class EmailContentBuilderPage : ContentBuilder() {

    private var address: String = ""
    private var body: String = ""
    private var subject: String = ""

    override fun getContentValue(): String {
        val email = BarcodeInfo.Email()
        email.address = address
        email.body = body
        email.subject = subject
        return email.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space(16.dp2px)
            Input(
                context.getString(R.string.email_address),
                InputConfig(
                    inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
                ),
                { address },
            ) {
                address = it.toString()
            }
            Space(16.dp2px)
            Input(
                context.getString(R.string.email_subject),
                InputConfig(
                    inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT),
                ),
                { subject },
            ) {
                subject = it.toString()
            }
            Input(
                context.getString(R.string.email_message),
                InputConfig(
                    inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
                        .or(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                ),
                { body },
            ) {
                body = it.toString()
            }
            Space(26.dp2px)
        }
    }
}