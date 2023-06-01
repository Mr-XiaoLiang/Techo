package com.lollipop.lqrdemo.creator.content.impl

import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class CalendarEventContentBuilderPage: ContentBuilder() {

    private var end: BarcodeInfo.CalendarDateTime = BarcodeInfo.CalendarDateTime()
    private var start: BarcodeInfo.CalendarDateTime = BarcodeInfo.CalendarDateTime()
    private var description by remember()
    private var location by remember()
    private var organizer by remember()
    private var status by remember()
    private var summary by remember()

    override fun getContentValue(): String {
        val calendarEvent = BarcodeInfo.CalendarEvent()
//        TODO("Not yet implemented")
//        var end: BarcodeInfo.CalendarDateTime = BarcodeInfo.CalendarDateTime()
//        var start: BarcodeInfo.CalendarDateTime = BarcodeInfo.CalendarDateTime()
//        var description: String = ""
//        var location: String = ""
//        var organizer: String = ""
//        var status: String = ""
//        var summary: String = ""
        return calendarEvent.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
//        TODO("Not yet implemented")
    }
}