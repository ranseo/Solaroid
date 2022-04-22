package com.example.solaroid.firebase

import com.example.solaroid.database.DatabasePhotoTicket


data class FirebasePhotoTicket(
    var url: String = "",
    var frontText : String = "",
    var backText: String ="",
    var date : String = "",
    var favorite : Boolean = false,
    var path : String = ""
) {

}

fun FirebasePhotoTicket.asDatabaseModel() : DatabasePhotoTicket {
    return DatabasePhotoTicket(
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        firebasePath = this.path
    )
}


data class FirebasePhotoTicketContainer(val photoTickets : List<FirebasePhotoTicket>)


fun FirebasePhotoTicketContainer.asDatabaseModel():  List<DatabasePhotoTicket> {
    return photoTickets.map {
        DatabasePhotoTicket(
            url = it.url,
            frontText = it.frontText,
            backText = it.backText,
            date = it.date,
            favorite = it.favorite,
            firebasePath = it.path
        )
    }
}
