package com.example.solaroid.firebase

data class PhotoTicketModel(
    var id : Long =0L,
    var uri: String? = null,
    var frontText : String = "",
    var backText: String ="",
    var date : String = "",
    var favorite : Boolean = false
) {

}