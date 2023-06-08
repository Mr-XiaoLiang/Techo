package com.lollipop.lqrdemo.creator.content.impl

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class CalendarEventContentBuilderPage : ContentBuilder() {

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
        space.apply {
            Space()
            Input(
                context.getString(R.string.calendar_summary),
                InputConfig.NORMAL,
                { summary },
            ) {
                summary = it
            }
            Space()
            SpaceEnd()
        }
    }

    private class DateItem(
        private val label: String,
        private val config: InputConfig,
        private val presetValue: () -> String,
        private val onInputChanged: (String) -> Unit,
    ) : Item() {
        override val viewId: Int
            get() = TODO("Not yet implemented")

        override fun updateChain(last: Item?, next: Item?) {
            TODO("Not yet implemented")
        }

        override fun bind(view: View) {
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->  }
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->  }
            TODO("Not yet implemented")
        }


    }

}