package com.example.solaroid.models.domain

import android.graphics.Bitmap
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.utils.BitmapUtils

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
    val thumbnail: Bitmap,
    val participant: String,
    val key: String
) {

}

fun RequestAlbum.asFirebaseModel() : FirebaseAlbum {
    return FirebaseAlbum(
        id,
        name,
        BitmapUtils.bitmapToString(thumbnail),
        participant,
        ""
    )
}
