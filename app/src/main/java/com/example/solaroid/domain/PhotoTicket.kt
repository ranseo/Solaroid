package com.example.solaroid.domain

import androidx.room.PrimaryKey
import com.example.solaroid.database.DatabasePhotoTicket
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

fun PhotoTicket.asDatabaseModel() : DatabasePhotoTicket {
    return DatabasePhotoTicket(
        key = this.id,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
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