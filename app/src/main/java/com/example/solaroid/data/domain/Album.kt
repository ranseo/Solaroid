package com.example.solaroid.data.domain

data class Album(
    val id:String,
    var name: String,
    var participants: List<Profile>,
    val photoTickets : List<PhotoTicket>,
    val key : String
) {

}

data class RequestAlbum(
    val id : String,
    val participants: List<Profile>,

) {

}