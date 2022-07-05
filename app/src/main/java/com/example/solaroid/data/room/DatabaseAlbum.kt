package com.example.solaroid.data.room

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.data.domain.Album
import com.example.solaroid.data.domain.PhotoTicket
import com.example.solaroid.data.domain.Profile
import com.example.solaroid.utils.BitmapUtils


@Entity(tableName = "album_table")
class DatabaseAlbum(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    @ColumnInfo(name = "album_name")
    var name: String,
    var thumbnail: ByteArray,
    var participants: List<DatabaseProfile>,
    val photoTickets : List<DatabasePhotoTicket>,
    val key: String
) {

}

fun DatabaseAlbum.asDomainModel() : Album {
    return Album(
        id,
        name,
        BitmapUtils.convertByteArrayToBitmap(thumbnail),
        participants.asDomainModel(),
        photoTickets.asDomainModel(),
    )
}

fun List<DatabaseAlbum>.asDomainModel() : List<Album> {
    return this.map {
        it.asDomainModel()
    }
}

@Entity(tableName = "home_table")
class DatabaseHome(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

)

