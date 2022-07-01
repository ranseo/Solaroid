package com.example.solaroid.domain

data class Album(
    var name: String,
    var participants: List<Profile>,
    val photoTickets : MutableList<PhotoTicket>,
    val key : String
) {

}