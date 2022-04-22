package com.example.solaroid.domain

import androidx.room.PrimaryKey

data class PhotoTicket(
    val id: Long= 0L,
    val url: String,
    var frontText : String,
    var backText: String,
    var date : String,
    var favorite : Boolean = false
) {

}