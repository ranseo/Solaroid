package com.example.solaroid.data.domain

import android.os.Parcelable
import com.example.solaroid.data.room.DatabasePhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicket
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoTicket(
    val id: String,
    val url: String,
    var frontText : String,
    var backText: String,
    var date : String,
    var favorite : Boolean = false
) : Parcelable {

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