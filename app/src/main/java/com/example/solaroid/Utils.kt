package com.example.solaroid


import android.content.res.Resources
import com.example.solaroid.database.PhotoTicket
import java.text.SimpleDateFormat
import java.util.*

fun convertTodayToFormatted(currentTimeMilli: Long, res: Resources): String {
    return SimpleDateFormat("yyyy.MM.dd.(E)", Locale.getDefault()).format(currentTimeMilli)
}

fun convertPhotoTicketToToastString(photoTicket: PhotoTicket, res:Resources) : String {
    return "${photoTicket.photo}\n${photoTicket.date}\n${photoTicket.frontText}\n${photoTicket.backText}"
}