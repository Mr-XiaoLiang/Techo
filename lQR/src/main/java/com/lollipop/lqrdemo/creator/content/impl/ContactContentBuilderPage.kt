package com.lollipop.lqrdemo.creator.content.impl

import androidx.annotation.StringRes
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
    private val addresses: ArrayList<BarcodeInfo.Address> = ArrayList()
    private val emails: ArrayList<BarcodeInfo.Email> = ArrayList()
    private val phones: ArrayList<BarcodeInfo.Phone> = ArrayList()
    private val urls: ArrayList<String> = ArrayList()

    override fun getContentValue(): String {
        val contact = BarcodeInfo.Contact()
//        TODO("Not yet implemented")
        return contact.getBarcodeValue()
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
            // 动态数量变化的item怎么办？
            SpaceEnd()
        }
    }

    private fun buildNameView(space: ItemSpace) {
        space.apply {
            InputByName(
                R.string.contact_prefix_name,
                { prefixName },
            ) { prefixName = it }
            InputByName(
                R.string.contact_first_name,
                { firstName },
            ) { firstName = it }
            InputByName(
                R.string.contact_middle_name,
                { middleName },
            ) { middleName = it }
            InputByName(
                R.string.contact_last_name,
                { lastName },
            ) { lastName = it }
            InputByName(
                R.string.contact_suffix_name,
                { suffixName },
            ) { suffixName = it }
            InputByName(
                R.string.contact_pronunciation_name,
                { pronunciationName },
            ) { pronunciationName = it }
        }
    }

    private fun ItemSpace.InputByName(
        @StringRes label: Int,
        presetValue: () -> String,
        onInputChanged: (String) -> Unit,
    ) = Input(
        label,
        InputConfig.NAME,
        presetValue,
        onInputChanged,
    )

}