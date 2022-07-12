package com.example.solaroid


import android.content.res.Resources
import com.example.solaroid.models.domain.PhotoTicket
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

fun convertLongToHexStringFormat(num:Long) : String {
    return "#"+"%04x".format(num)
}

fun convertHexStringToLongFormat(str:String) : Long {
    return str.substring(1..4).toLong(16)
}

fun getAlbumIdWithFriendCodes(friendCodes:List<String>, albumNumbering:Int) : String {
    return friendCodes.fold("") { acc, v ->
        acc + v
    } + "N.${albumNumbering}"
}

fun getAlbumNameWithFriendsNickname(nickname:List<String>) : String {
    return nickname.fold("") { acc, v ->
        "$acc\'$v\'님, "
    }.dropLast(2) + "의 앨범"
}

fun getAlbumPariticipantsWithFriendCodes(friendCodes:List<String>) : String {
    return friendCodes.fold("") { acc, v ->
        acc + v
    }
}

fun parseProfileImgStringToList(profiles : String) : List<String> {
    return profiles.split("||")
}





