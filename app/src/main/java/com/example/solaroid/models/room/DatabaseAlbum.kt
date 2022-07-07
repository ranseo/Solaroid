package com.example.solaroid.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.models.domain.Album
import com.example.solaroid.utils.BitmapUtils


@Entity(tableName = "album_table")
class DatabaseAlbum(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "album_name")
    var name: String,
    var thumbnail: ByteArray,
    val participants:String,
    val key: String
) {

}

fun DatabaseAlbum.asDomainModel() : Album {
    return Album(
        id,
        name,
        BitmapUtils.convertByteArrayToBitmap(thumbnail),
        participants
    )
}

fun List<DatabaseAlbum>.asDomainModel() : List<Album> {
    return this.map{
        it.asDomainModel()
    }

}



@Entity(tableName = "home_table")
class DatabaseHome(
    @PrimaryKey(autoGenerate = true)
    val albumId: String

)

