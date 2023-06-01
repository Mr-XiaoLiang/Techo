package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class ContactContentBuilderPage: ContentBuilder() {

    private var name: BarcodeInfo.PersonName = BarcodeInfo.PersonName()
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
//        TODO("Not yet implemented")
    }
}