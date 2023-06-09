package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class EmailContentBuilderPage : ContentBuilder() {

    private var address by remember()
    private var body by remember()
    private var subject by remember()

    override fun getContentValue(): String {
        val email = BarcodeInfo.Email()
        email.address = address
        email.body = body
        email.subject = subject
        return email.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.email_address,
                InputConfig.EMAIL,
                { address },
            ) {
                address = it
            }
            Space()
            Input(
                R.string.email_subject,
                InputConfig.SUBJECT,
                { subject },
            ) {
                subject = it
            }
            Input(
                R.string.email_message,
                InputConfig.CONTENT,
                { body },
            ) {
                body = it
            }
            SpaceEnd()
        }
    }
}