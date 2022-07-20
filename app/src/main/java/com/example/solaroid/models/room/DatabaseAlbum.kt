package com.example.solaroid.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.parseAlbumIdDomainToFirebase
import com.example.solaroid.utils.BitmapUtils


@Entity(tableName = "album_table")
data class DatabaseAlbum(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "album_name")
    var name: String,
    var thumbnail: String,
    val participants:String,
    val key: String
) {
    fun getAlbumIdForFirebase() = parseAlbumIdDomainToFirebase(id,key)
}

fun DatabaseAlbum.asDomainModel() : Album {
    return Album(
        id,
        name,
        BitmapUtils.stringToBitmap(thumbnail),
        participants,
    )
}

fun DatabaseAlbum.asFirebaseModel() : FirebaseAlbum =
    FirebaseAlbum(
        parseAlbumIdDomainToFirebase(id,key),
        name,
        thumbnail,
        participants,
        key
    )


fun List<DatabaseAlbum>.asDomainModel() : List<Album> {
    return this.map{
        it.asDomainModel()
    }

}



fun DatabaseAlbum.asHomeAlbum() : DatabaseHomeAlbum {
    return DatabaseHomeAlbum(
        true,
        name,
        id
    )
}



