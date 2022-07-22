package com.example.solaroid.models.domain

import android.os.Parcelable
import com.example.solaroid.models.room.DatabasePhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.parseAlbumIdDomainToFirebase
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoTicket(
    val id: String,
    val url: String,
    var frontText : String,
    var backText: String,
    var date : String,
    var favorite : Boolean = false,
    var albumInfo : List<String>
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
        user = user,
        albumId = albumInfo[0],
        albumKey = albumInfo[1],
        albumName = albumInfo[2]
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
        albumId = parseAlbumIdDomainToFirebase(albumInfo[0], albumInfo[1]),
        albumKey = albumInfo[1],
        albumName = albumInfo[2]
    )
}

//fun List<PhotoTicket>.asFirebaseModel() : List<FirebasePhotoTicket> {
//    return this.map{
//        it.asFirebaseModel()
//    }
//}