package com.ranseo.solaroid.firebase

import com.ranseo.solaroid.joinAlbumIdAndKey
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.models.room.DatabasePhotoTicket


data class FirebasePhotoTicket(
    var key : String = "",
    var url: String = "",
    var frontText : String = "",
    var backText: String ="",
    var date : String = "",
    var favorite : Boolean = false,
    val albumName: String,
    val albumId: String,
    val albumKey: String
) {

}

fun FirebasePhotoTicket.asDatabaseModel(user:String) : DatabasePhotoTicket {
    return DatabasePhotoTicket(
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        key = this.key,
        user = user,
        albumName = albumName,
        albumKey = albumKey,
        albumId = joinAlbumIdAndKey(albumId, albumKey)
    )
}

fun List<FirebasePhotoTicket>.asDatabaseModel(user:String) : List<DatabasePhotoTicket> {
    return this.map {
        it.asDatabaseModel(user)
    }
}

fun FirebasePhotoTicket.asDomainModel() : PhotoTicket {
    return PhotoTicket(
        key,
        url,
        frontText,
        backText,
        date,
        favorite,
        albumInfo = listOf(albumId, albumKey, albumName)
    )
}

fun List<FirebasePhotoTicket>.asDomainModel() : List<PhotoTicket> {
    return this.map {
        it.asDomainModel()
    }
}


data class FirebasePhotoTicketContainer(val photoTickets : List<FirebasePhotoTicket>)


fun FirebasePhotoTicketContainer.asDatabaseModel(user:String):  List<DatabasePhotoTicket> {
    return photoTickets.map { it.asDatabaseModel(user) }
}
