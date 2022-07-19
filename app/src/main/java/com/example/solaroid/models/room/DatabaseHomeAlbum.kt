package com.example.solaroid.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.models.domain.Album

@Entity(tableName = "home_table")
data class DatabaseHomeAlbum(
    @PrimaryKey
    val id : Boolean = true,
    @ColumnInfo(name="home_album_name")
    val name: String,
    val albumId: String
)
