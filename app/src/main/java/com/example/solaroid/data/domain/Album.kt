package com.example.solaroid.data.domain

import android.graphics.Bitmap
import com.example.solaroid.data.room.DatabaseAlbum

data class Album(
    val id:String,
    var name: String,
    var thumbnail: Bitmap,
    var participants: List<Profile>,
    val photoTickets : List<PhotoTicket>,
) {

}

data class RequestAlbum(
    val id : String,
    val thumbnail: Bitmap,
    val participants: List<Profile>,

) {

}
