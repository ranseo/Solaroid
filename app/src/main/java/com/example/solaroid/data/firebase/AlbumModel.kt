package com.example.solaroid.data.firebase

import com.example.solaroid.data.domain.PhotoTicket
import com.example.solaroid.data.domain.Profile

data class FirebaseAlbum(
    val id: String,
    val name: String,
    val participants:List<Profile>,
    val photoTickets:List<PhotoTicket>,
    val key: String
) {
}