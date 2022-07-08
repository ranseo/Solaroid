package com.example.solaroid.repositery.album

import androidx.lifecycle.Transformations
import com.example.solaroid.models.room.asDomainModel
import com.example.solaroid.room.DatabasePhotoTicketDao

class HomeAlbumRepositery(
    roomDB: DatabasePhotoTicketDao
) {

    private val homeAlbum = Transformations.switchMap(roomDB.getHomeAlbum()) {
        roomDB.getAlbum(it.albumId)
    }

    val album = Transformations.map(homeAlbum) {
        it.asDomainModel()
    }




}