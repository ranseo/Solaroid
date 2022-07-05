package com.example.solaroid.models.domain

import android.graphics.Bitmap

data class Album(
    val id:String,
    var name: String,
    var thumbnail: Bitmap,
    val participant: List<Profile>
) {

}



data class RequestAlbum(
    val id : String,
    val name :String

) {

}
