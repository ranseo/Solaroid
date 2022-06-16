package com.example.solaroid.domain

import com.example.solaroid.room.DatabasePhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicket

data class PhotoTicket(
    val id: String,
    val url: String,
    var frontText : String,
    var backText: String,
    var date : String,
    var favorite : Boolean = false
) {

}

fun PhotoTicket.asDatabaseModel(user:String) : DatabasePhotoTicket {
    return DatabasePhotoTicket(
        key = this.id,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        user = user
    )
}

fun PhotoTicket.asFirebaseModel(key:String) : FirebasePhotoTicket {
    return FirebasePhotoTicket(
        key = key,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
    )
}