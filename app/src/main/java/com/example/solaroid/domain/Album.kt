package com.example.solaroid.domain

data class Album(
    var name: String,
    var participants: List<Profile>,
    var isHome : Boolean,

    val key : String
) {

}