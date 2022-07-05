package com.example.solaroid.firebase

import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.models.room.DatabasePhotoTicket


data class FirebasePhotoTicket(
    var key : String = "",
    var url: String = "",
    var frontText : String = "",
    var backText: String ="",
    var date : String = "",
    var favorite : Boolean = false
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
        user = user
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
        favorite
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
