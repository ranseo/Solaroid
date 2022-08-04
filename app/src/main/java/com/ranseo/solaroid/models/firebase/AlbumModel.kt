package com.ranseo.solaroid.models.firebase

import com.ranseo.solaroid.models.room.DatabaseAlbum
import com.ranseo.solaroid.joinAlbumIdAndKey
import com.ranseo.solaroid.models.domain.RequestAlbum
import com.ranseo.solaroid.utils.BitmapUtils

data class FirebaseAlbum(
    val id: String,
    val name: String,
    val thumbnail: String,
    val participants:String,
    val numOfParticipants: Long,
    val key: String
) {
}



fun FirebaseAlbum.asDatabaseModel(user:String) : DatabaseAlbum {
    return DatabaseAlbum(
        joinAlbumIdAndKey(id,key),
        name,
        thumbnail,
        participants,
        numOfParticipants.toInt(),
        key,
        user
    )
}



data class FirebaseRequestAlbum(
    val id : String,
    val name: String,
    val thumbnail: String,
    val participants: String,
    val numOfParticipants: Long,
    val albumKey: String,
    val key: String
    ) {

}

fun FirebaseRequestAlbum.asDomainModel() : RequestAlbum {
    return RequestAlbum(
        id,
        name,
        BitmapUtils.stringToBitmap(thumbnail),
        participants,
        numOfParticipants.toInt(),
        albumKey,
        key
    )
}