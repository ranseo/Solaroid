package com.example.solaroid.data.firebase

import androidx.room.Database
import com.example.solaroid.data.domain.Album
import com.example.solaroid.data.domain.Profile
import com.example.solaroid.data.room.DatabaseAlbum
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.utils.BitmapUtils

data class FirebaseAlbum(
    val id: String,
    val name: String,
    val thumbnail: ByteArray,
    val participants:List<FirebaseProfile>,
    val photoTickets:List<FirebasePhotoTicket>,
    val key: String
) {
}

fun FirebaseAlbum.asDatabaseModel(user:String) : DatabaseAlbum {
    return DatabaseAlbum(
        id,
        name,
        thumbnail,
        participants.asDatabaseModel(),
        photoTickets.asDatabaseModel(user),
        key
    )
}



data class FirebaseRequestAlbum(
    val id : String,
    val name: String,
    val thumbnail: ByteArray,
    val participants: List<Profile>,

    ) {

}