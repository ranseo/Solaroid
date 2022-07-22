package com.example.solaroid


import android.content.res.Resources
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.PhotoTicket
import java.text.SimpleDateFormat
import java.util.*

fun convertTodayToFormatted(currentTimeMilli: Long): String {
    return SimpleDateFormat("yyyy.MM.dd(E).kk.mm.ss", Locale.getDefault()).format(currentTimeMilli)
}

fun convertDateToLong(year: Int, month: Int, day: Int): Long {
    val gc = GregorianCalendar(year, month, day)
    return gc.timeInMillis
}

fun convertPhotoTicketToToastString(photoTicket: PhotoTicket, res: Resources): String {
    return "${photoTicket.url}\n${photoTicket.date}\n${photoTicket.frontText}\n${photoTicket.backText}"
}

fun convertLongToHexStringFormat(num: Long): String {
    return "#" + "%04x".format(num)
}

fun convertHexStringToLongFormat(str: String): Long {
    return str.substring(1..4).toLong(16)
}


fun getAlbumIdWithFriendCodes(friendCodes: List<String>): String {
    return friendCodes.fold("") { acc, v ->
        acc + v.drop(1) + "|"
    }.dropLast(1)
}

fun getAlbumNameWithFriendsNickname(nickname: List<String>, myNickname:String): String {
    return nickname.fold("$myNickname, ") { acc, v ->
        "$acc\'$v\'님, "
    }.dropLast(2) + "의 앨범"
}

fun getAlbumNameWithFriendsNickname(nickname: List<String>): String {
    return nickname.fold("") { acc, v ->
        "$acc\'$v\'님, "
    }.dropLast(2) + "의 앨범"
}

fun getAlbumParticipantsWithFriendCodes(friendCodes: List<String>): String {
    return friendCodes.fold("") { acc, v ->
        "$acc$v||"
    }.dropLast(2)
}

fun getAlbumParticipantsWithFriendCodes(myFriendCode:String,friendCodes: List<String>): String {
    return friendCodes.fold("$myFriendCode||") { acc, v ->
        "$acc$v||"
    }.dropLast(2)
}

fun parseProfileImgStringToList(profiles: String): List<String> {
    return profiles.split("||")
}


fun joinProfileImgListToString(participants: List<String>) : String{
    return participants.fold("") { acc, v -> "$acc$v||"}.dropLast(2)
}


fun parseAlbumIdDomainToFirebase(albumId:String, key:String) : String {
    return albumId.removeSuffix(key).dropLast(2)
}

fun joinAlbumIdAndKey(albumId:String, albumKey:String) : String {
    return "$albumId||$albumKey"

}





