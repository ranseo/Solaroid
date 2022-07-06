package com.example.solaroid.models.domain

import android.graphics.Bitmap

data class Album(
    val id:String,
    var name: String,
    var thumbnail: Bitmap,
    val participant: String
) {

}



data class RequestAlbum(
    val id : String,
    val name :String,
    val participant: String
) {

}
