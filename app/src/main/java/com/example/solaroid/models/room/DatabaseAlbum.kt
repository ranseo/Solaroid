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
    @ColumnInfo(name = "album_id")
    val id: String,
    @ColumnInfo(name = "album_name")
    var name: String,
    var thumbnail: String,
    val participants:String,
    val numOfParticipants: Int,
    val key: String,
    @ColumnInfo(name ="album_user")
    val user:String
) {
    fun getAlbumIdForFirebase() = parseAlbumIdDomainToFirebase(id,key)
}

fun DatabaseAlbum.asDomainModel() : Album {
    return Album(
        id,
        name,
        BitmapUtils.stringToBitmap(thumbnail),
        participants,
        numOfParticipants
    )
}

fun DatabaseAlbum.asFirebaseModel() : FirebaseAlbum =
    FirebaseAlbum(
        parseAlbumIdDomainToFirebase(id,key),
        name,
        thumbnail,
        participants,
        numOfParticipants,
        key
    )


fun DatabaseAlbum.asDatabaseAlbum(name:String) =
    DatabaseAlbum(
        this.id,
        name,
        this.thumbnail,
        this.participants,
        this.numOfParticipants,
        this.key,
        this.user
    )

fun List<DatabaseAlbum>.asDomainModel() : List<Album> {
    return this.map{
        it.asDomainModel()
    }
}

fun List<DatabaseAlbum>.modifyOverrideAlbumName() : List<DatabaseAlbum> {
    val hash = this.groupBy { it.name }
    var list = listOf<DatabaseAlbum>()
    for(key in hash.keys) {
        var cnt = 1
        if(hash[key]?.size!! > 1) list+= hash[key]?.map{album -> album.asDatabaseAlbum("${album.name} (${cnt++})")} as List<DatabaseAlbum>
        else list += hash[key]!!
    }
    return list
}
