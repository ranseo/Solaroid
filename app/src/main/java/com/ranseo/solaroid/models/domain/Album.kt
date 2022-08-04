package com.ranseo.solaroid.models.domain

import android.graphics.Bitmap
import com.ranseo.solaroid.models.firebase.FirebaseAlbum
import com.ranseo.solaroid.utils.BitmapUtils

data class Album(
    val id:String,
    var name: String,
    var thumbnail: Bitmap,
    val participant: String,
    val numOfParticipants: Int
) {

}



data class RequestAlbum(
    val id : String,
    val name :String,
    val thumbnail: Bitmap,
    val participant: String,
    val numOfParticipants: Int,
    val albumKey:String,
    val key: String
) {

}

fun RequestAlbum.asFirebaseModel() : FirebaseAlbum {
    return FirebaseAlbum(
        id,
        name,
        BitmapUtils.bitmapToString(thumbnail),
        participant,
        numOfParticipants.toLong(),
        albumKey
    )
}
