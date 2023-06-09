package com.lollipop.lqrdemo.creator.content.impl

import android.os.Bundle
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo
import java.util.Calendar

class CalendarEventContentBuilderPage : ContentBuilder() {

    private var startYear by rememberInt()
    private var startMonth by rememberInt()
    private var startDay by rememberInt()
    private var startHours by rememberInt()
    private var startMinutes by rememberInt()
    private var startSeconds by rememberInt()

    private var endYear by rememberInt()
    private var endMonth by rememberInt()
    private var endDay by rememberInt()
    private var endHours by rememberInt()
    private var endMinutes by rememberInt()
    private var endSeconds by rememberInt()

    private var description by remember()
    private var location by remember()
    private var organizer by remember()
    private var status by remember()
    private var summary by remember()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if (startYear == 0) {
            startYear = calendar.get(Calendar.YEAR)
        }
        if (startMonth == 0) {
            startMonth = calendar.get(Calendar.MONTH) + 1
        }
        if (startDay == 0) {
            startDay = calendar.get(Calendar.DAY_OF_MONTH)
        }
        if (startHours == 0) {
            startHours = calendar.get(Calendar.HOUR_OF_DAY)
        }
        if (startMinutes == 0) {
            startMinutes = calendar.get(Calendar.MINUTE)
        }
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        if (endYear == 0) {
            endYear = calendar.get(Calendar.YEAR)
        }
        if (endMonth == 0) {
            endMonth = calendar.get(Calendar.MONTH) + 1
        }
        if (endDay == 0) {
            endDay = calendar.get(Calendar.DAY_OF_MONTH)
        }
        if (endHours == 0) {
            endHours = calendar.get(Calendar.HOUR_OF_DAY)
        }
        if (endMinutes == 0) {
            endMinutes = calendar.get(Calendar.MINUTE)
        }
    }

    override fun getContentValue(): String {
        val calendarEvent = BarcodeInfo.CalendarEvent()
        calendarEvent.start = BarcodeInfo.CalendarDateTime(
            year = startYear,
            month = startMonth,
            day = startDay,
            hours = startHours,
            minutes = startMinutes,
            seconds = startSeconds
        )
        calendarEvent.end = BarcodeInfo.CalendarDateTime(
            year = endYear,
            month = endMonth,
            day = endDay,
            hours = endHours,
            minutes = endMinutes,
            seconds = endSeconds
        )
        calendarEvent.description = description
        calendarEvent.location = location
        calendarEvent.organizer = organizer
        calendarEvent.status = status
        calendarEvent.summary = summary
        return calendarEvent.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.calendar_summary,
                InputConfig.NORMAL,
                { summary },
            ) {
                summary = it
            }
            Space()
            Date(
                R.string.calendar_start_time,
                {
                    DateInfo(
                        startYear,
                        startMonth,
                        startDay,
                        startHours,
                        startMinutes,
                        startSeconds
                    )
                }
            ) {
                startYear = it.year
                startMonth = it.month
                startDay = it.day
                startHours = it.hours
                startMinutes = it.minutes
                startSeconds = it.seconds
                checkTime(false)
            }
            Date(
                R.string.calendar_end_time,
                {
                    DateInfo(
                        endYear,
                        endMonth,
                        endDay,
                        endHours,
                        endMinutes,
                        endSeconds
                    )
                }
            ) {
                endYear = it.year
                endMonth = it.month
                endDay = it.day
                endHours = it.hours
                endMinutes = it.minutes
                endSeconds = it.seconds
                checkTime(true)
            }
            Space()
            Input(
                R.string.calendar_status,
                InputConfig.NORMAL,
                { status },
            ) {
                status = it
            }
            Input(
                R.string.calendar_organizer,
                InputConfig.NORMAL,
                { organizer },
            ) {
                organizer = it
            }
            Input(
                R.string.calendar_location,
                InputConfig.NORMAL,
                { location },
            ) {
                location = it
            }
            Space()
            Input(
                R.string.calendar_description,
                InputConfig.CONTENT,
                { description },
            ) {
                description = it
            }
            SpaceEnd()
        }
    }

    private fun checkTime(changeStart: Boolean) {
        val calendar = Calendar.getInstance()
        val startTime = getTime(
            calendar,
            startYear,
            startMonth,
            startDay,
            startHours,
            startMinutes,
            startSeconds
        )
        val endTime = getTime(
            calendar,
            endYear,
            endMonth,
            endDay,
            endHours,
            endMinutes,
            endSeconds
        )
        if (startTime > endTime) {
            if (changeStart) {
                startYear = endYear
                startMonth = endMonth
                startDay = endDay
                startHours = endHours
                startMinutes = endMinutes
                startSeconds = endSeconds
            } else {
                endYear = startYear
                endMonth = startMonth
                endDay = startDay
                endHours = startHours
                endMinutes = startMinutes
                endSeconds = startSeconds
            }
            notifyStateChanged()
        }
    }

    private fun getTime(
        calendar: Calendar,
        year: Int,
        month: Int,
        day: Int,
        hours: Int,
        minute: Int,
        second: Int
    ): Long {
        calendar.set(year, month, day, hours, minute, second)
        return calendar.timeInMillis
    }

}