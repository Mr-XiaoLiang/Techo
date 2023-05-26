package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.lollipop.qr.comm.BarcodeInfo
import java.util.Calendar

object CalendarEventRouter : BarcodeRouter<BarcodeInfo.CalendarEvent>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.CalendarEvent): Intent {
        val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
        buildInfo(intent, barcodeInfo)
        return intent
    }

    private fun buildInfo(intent: Intent, barcodeInfo: BarcodeInfo.CalendarEvent) {
        intent.putExtra(CalendarContract.Events.TITLE, barcodeInfo.summary)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, barcodeInfo.description)
        intent.putExtra(CalendarContract.Events.STATUS, barcodeInfo.status)
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, barcodeInfo.location)
        intent.putExtra(CalendarContract.Events.ORGANIZER, barcodeInfo.organizer)

        val calendar = Calendar.getInstance()
        val start = barcodeInfo.start
        calendar.set(Calendar.YEAR, start.year)
        calendar.set(Calendar.MONTH, start.month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, start.day)
        calendar.set(Calendar.HOUR_OF_DAY, start.hours)
        calendar.set(Calendar.MINUTE, start.minutes)
        calendar.set(Calendar.SECOND, start.seconds)

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)

        val end = barcodeInfo.end
        calendar.set(Calendar.YEAR, end.year)
        calendar.set(Calendar.MONTH, end.month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, end.day)
        calendar.set(Calendar.HOUR_OF_DAY, end.hours)
        calendar.set(Calendar.MINUTE, end.minutes)
        calendar.set(Calendar.SECOND, end.seconds)

        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis)
    }

}