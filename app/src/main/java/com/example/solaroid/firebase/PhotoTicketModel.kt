package com.example.solaroid.firebase

import com.example.solaroid.database.DatabasePhotoTicket


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


data class FirebasePhotoTicketContainer(val photoTickets : List<FirebasePhotoTicket>)


fun FirebasePhotoTicketContainer.asDatabaseModel(user:String):  List<DatabasePhotoTicket> {
    return photoTickets.map { it.asDatabaseModel(user) }
}
