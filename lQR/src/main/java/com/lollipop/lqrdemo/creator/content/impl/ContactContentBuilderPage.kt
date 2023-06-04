package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class ContactContentBuilderPage : ContentBuilder() {

    private var prefixName by remember()
    private var firstName by remember()
    private var middleName by remember()
    private var lastName by remember()
    private var suffixName by remember()
    private var pronunciationName by remember()

    private var organization by remember()
    private var title by remember()

    private var phone1 by remember()
    private var phone2 by remember()
    private var phone3 by remember()

    private var email1 by remember()
    private var email2 by remember()
    private var email3 by remember()

    private var url1 by remember()
    private var url2 by remember()
    private var url3 by remember()

    private var address1 by remember()
    private var address2 by remember()
    private var address3 by remember()

    override fun getContentValue(): String {
        val contact = BarcodeInfo.Contact()
        contact.name = BarcodeInfo.PersonName(
            prefix = prefixName,
            first = firstName,
            middle = middleName,
            last = lastName,
            suffix = suffixName,
            pronunciation = pronunciationName
        )
        contact.organization = organization
        contact.title = title
        tryAddPhone(phone1, contact.phones)
        tryAddPhone(phone2, contact.phones)
        tryAddPhone(phone3, contact.phones)

        tryAddEmail(email1, contact.emails)
        tryAddEmail(email2, contact.emails)
        tryAddEmail(email3, contact.emails)

        tryAddUrl(url1, contact.urls)
        tryAddUrl(url2, contact.urls)
        tryAddUrl(url3, contact.urls)

        tryAddAddress(address1, contact.addresses)
        tryAddAddress(address2, contact.addresses)
        tryAddAddress(address3, contact.addresses)
        return contact.getBarcodeValue()
    }

    private fun tryAddAddress(value: String, list: MutableList<BarcodeInfo.Address>) {
        if (value.isNotEmpty()) {
            val splitLine = value.split("\n").toTypedArray()
            list.add(BarcodeInfo.Address(lines = splitLine))
        }
    }


    private fun tryAddUrl(value: String, list: MutableList<String>) {
        if (value.isNotEmpty()) {
            list.add(value)
        }
    }


    private fun tryAddEmail(value: String, list: MutableList<BarcodeInfo.Email>) {
        if (value.isNotEmpty()) {
            list.add(
                BarcodeInfo.Email().apply {
                    address = value
                    type = BarcodeInfo.Email.Type.UNKNOWN
                }
            )
        }
    }

    private fun tryAddPhone(value: String, list: MutableList<BarcodeInfo.Phone>) {
        if (value.isNotEmpty()) {
            list.add(
                BarcodeInfo.Phone().apply {
                    number = value
                    type = BarcodeInfo.Phone.Type.MOBILE
                }
            )
        }
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.contact_title,
                InputConfig.NORMAL,
                { title },
            ) { title = it }
            Space()
            Input(
                R.string.contact_organization,
                InputConfig.NORMAL,
                { organization },
            ) { organization = it }
            Space()
            buildNameView(this)
            Space()
            buildPhoneView(this)
            Space()
            buildEmailView(this)
            Space()
            buildUrlView(this)
            Space()
            buildAddressView(this)
            SpaceEnd()
        }
    }

    private fun buildPhoneView(space: ItemSpace) {
        space.apply {
            Input(
                R.string.contact_phone_1,
                InputConfig.PHONE,
                { phone1 }
            ) { phone1 = it }
            Input(
                R.string.contact_phone_2,
                InputConfig.PHONE,
                { phone2 }
            ) { phone2 = it }
            Input(
                R.string.contact_phone_3,
                InputConfig.PHONE,
                { phone3 }
            ) { phone3 = it }
        }
    }

    private fun buildAddressView(space: ItemSpace) {
        space.apply {
            Input(
                R.string.contact_address_1,
                InputConfig.CONTENT,
                { address1 }
            ) { address1 = it }
            Input(
                R.string.contact_address_2,
                InputConfig.CONTENT,
                { address2 }
            ) { address2 = it }
            Input(
                R.string.contact_address_3,
                InputConfig.CONTENT,
                { address3 }
            ) { address3 = it }
        }
    }

    private fun buildEmailView(space: ItemSpace) {
        space.apply {
            Input(
                R.string.contact_email_1,
                InputConfig.EMAIL,
                { email1 }
            ) { email1 = it }
            Input(
                R.string.contact_email_2,
                InputConfig.EMAIL,
                { email2 }
            ) { email2 = it }
            Input(
                R.string.contact_email_3,
                InputConfig.EMAIL,
                { email3 }
            ) { email3 = it }
        }
    }

    private fun buildUrlView(space: ItemSpace) {
        space.apply {
            Input(
                R.string.contact_url_1,
                InputConfig.URL,
                { url1 }
            ) { url1 = it }
            Input(
                R.string.contact_url_2,
                InputConfig.URL,
                { url2 }
            ) { url2 = it }
            Input(
                R.string.contact_url_3,
                InputConfig.URL,
                { url3 }
            ) { url3 = it }
        }
    }

    private fun buildNameView(space: ItemSpace) {
        space.apply {
            Input(
                R.string.contact_prefix_name,
                InputConfig.NAME,
                { prefixName },
            ) { prefixName = it }
            Input(
                R.string.contact_first_name,
                InputConfig.NAME,
                { firstName },
            ) { firstName = it }
            Input(
                R.string.contact_middle_name,
                InputConfig.NAME,
                { middleName },
            ) { middleName = it }
            Input(
                R.string.contact_last_name,
                InputConfig.NAME,
                { lastName },
            ) { lastName = it }
            Input(
                R.string.contact_suffix_name,
                InputConfig.NAME,
                { suffixName },
            ) { suffixName = it }
            Input(
                R.string.contact_pronunciation_name,
                InputConfig.NAME,
                { pronunciationName },
            ) { pronunciationName = it }
        }
    }

}