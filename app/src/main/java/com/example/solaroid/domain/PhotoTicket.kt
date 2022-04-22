package com.example.solaroid.domain

import androidx.room.PrimaryKey
import com.example.solaroid.database.DatabasePhotoTicket

data class PhotoTicket(
    val id: Long= 0L,
    val url: String,
    var frontText : String,
    var backText: String,
    var date : String,
    var favorite : Boolean = false
) {

}

fun PhotoTicket.asDatabaseModel(key: String) : DatabasePhotoTicket {
    return DatabasePhotoTicket(
        id = this.id,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        firebaseKey = key
    )
}