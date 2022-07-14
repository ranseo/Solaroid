package com.example.solaroid.models.firebase

import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.models.domain.RequestAlbum

data class FirebaseAlbum(
    val id: String,
    val name: String,
    val thumbnail: String,
    val participants:String,
    val key: String
) {
}



fun FirebaseAlbum.asDatabaseModel() : DatabaseAlbum {
    return DatabaseAlbum(
        id,
        name,
        thumbnail,
        participants,
        key
    )
}



data class FirebaseRequestAlbum(
    val id : String,
    val name: String,
    val participants: String,
    val key: String
    ) {

}

fun FirebaseRequestAlbum.asDomainModel() : RequestAlbum {
    return RequestAlbum(
        id,
        name,
        participants
    )
}