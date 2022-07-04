package com.example.solaroid.data.domain

data class Album(
    val id:String,
    var name: String,
    var participants: List<Profile>,
    val photoTickets : MutableList<PhotoTicket>,
    val key : String
) {

}