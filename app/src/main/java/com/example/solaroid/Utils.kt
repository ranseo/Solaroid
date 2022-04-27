package com.example.solaroid


import android.content.res.Resources
import com.example.solaroid.domain.PhotoTicket
import java.text.SimpleDateFormat
import java.util.*

fun convertTodayToFormatted(currentTimeMilli: Long): String {
    return SimpleDateFormat("yyyy.MM.dd(E).kk.mm.ss", Locale.getDefault()).format(currentTimeMilli)
}

fun convertPhotoTicketToToastString(photoTicket: PhotoTicket, res:Resources) : String {
    return "${photoTicket.url}\n${photoTicket.date}\n${photoTicket.frontText}\n${photoTicket.backText}"
}