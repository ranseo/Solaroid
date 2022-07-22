package com.example.solaroid.models.firebase

import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.joinAlbumIdAndKey
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.utils.BitmapUtils

data class FirebaseAlbum(
    val id: String,
    val name: String,
    val thumbnail: String,
    val participants:String,
    val key: String
) {
}



fun FirebaseAlbum.asDatabaseModel(user:String) : DatabaseAlbum {
    return DatabaseAlbum(
        joinAlbumIdAndKey(id,key),
        name,
        thumbnail,
        participants,
        key,
        user
    )
}



data class FirebaseRequestAlbum(
    val id : String,
    val name: String,
    val thumbnail: String,
    val participants: String,
    val key: String
    ) {

}

fun FirebaseRequestAlbum.asDomainModel() : RequestAlbum {
    return RequestAlbum(
        id,
        name,
        BitmapUtils.stringToBitmap(thumbnail),
        participants,
        key
    )
}