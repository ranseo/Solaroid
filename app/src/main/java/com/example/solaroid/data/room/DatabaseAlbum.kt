package com.example.solaroid.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.data.domain.PhotoTicket
import com.example.solaroid.data.domain.Profile


@Entity(tableName = "album_table")
class DatabaseAlbum(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    var name: String,
    var participants: List<Profile>,
    val photoTickets : List<PhotoTicket>,
    var isHome : Boolean,
    val key: String
) {

} 