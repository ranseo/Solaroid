package com.example.solaroid


import android.content.res.Resources
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun convertTodayToFormatted(currentTimeMilli : Long, res: Resources): String {
    val weekdayString = SimpleDateFormat("yyyy.MM.DD.(E)", Locale.getDefault()).format(currentTimeMilli)
    return weekdayString
}

fun convertPhotoTicketToToastString(photoTicket: PhotoTicket, res:Resources) : String {

    return "${photoTicket.photo}\n${photoTicket.date}\n${photoTicket.frontText}\n${photoTicket.backText}"
}