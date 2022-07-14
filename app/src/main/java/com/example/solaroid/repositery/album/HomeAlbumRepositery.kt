package com.example.solaroid.repositery.album

import androidx.lifecycle.Transformations
import com.example.solaroid.models.room.DatabaseHomeAlbum
import com.example.solaroid.models.room.asDomainModel
import com.example.solaroid.room.DatabasePhotoTicketDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeAlbumRepositery(
    val roomDB: DatabasePhotoTicketDao
) {

     val homeAlbumId = Transformations.map(roomDB.getHomeAlbum()) {
        it.albumId
    }
//
//    val albumKey = Transformations.map(homeAlbum) {
//        it.key
//    }
//
//    val album = Transformations.map(homeAlbum) {
//        it.asDomainModel()
//    }

    suspend fun insertRoomHomeAlbum(homeAlbum: DatabaseHomeAlbum) {
        withContext(Dispatchers.IO) {
            roomDB.insert(homeAlbum)
        }
    }




}