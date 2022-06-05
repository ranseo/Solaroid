package com.example.solaroid


import android.content.res.Resources
import com.example.solaroid.domain.PhotoTicket
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun convertTodayToFormatted(currentTimeMilli: Long): String {
    return SimpleDateFormat("yyyy.MM.dd(E).kk.mm.ss", Locale.getDefault()).format(currentTimeMilli)
}

fun convertDateToLong(year:Int,month:Int,day:Int) :Long{
    val gc = GregorianCalendar(year,month,day)
    return gc.timeInMillis
}

fun convertPhotoTicketToToastString(photoTicket: PhotoTicket, res:Resources) : String {
    return "${photoTicket.url}\n${photoTicket.date}\n${photoTicket.frontText}\n${photoTicket.backText}"
}

fun convertLongToDecimalFormat(num:Long) : String {
    return "#"+"%04x".format(num)
}