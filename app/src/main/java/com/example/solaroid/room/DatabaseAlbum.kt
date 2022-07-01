package com.example.solaroid.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.domain.Profile

@Entity(tableName = "album_table")
class DatabaseAlbum(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var name: String,
    var participants: List<Profile>,
    val photoTickets : MutableList<PhotoTicket>,
    var isHome : Boolean,
    val key: String
) {

} 