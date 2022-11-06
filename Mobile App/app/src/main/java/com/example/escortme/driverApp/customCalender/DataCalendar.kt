package com.example.escortme.driverApp.customCalender

import java.text.SimpleDateFormat
import java.util.*

data class DataCalendar(var data: Date, var selected: Boolean = false) {

    val day: String
        get() = SimpleDateFormat("EE", Locale.getDefault()).format(data)

    val date: String
        get() {
            val cal = Calendar.getInstance()
            cal.time = data
            return cal[Calendar.DAY_OF_MONTH].toString()
        }
}