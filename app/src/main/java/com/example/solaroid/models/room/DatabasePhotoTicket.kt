package com.example.solaroid.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.parseAlbumIdDomainToFirebase

//변수 photo는 임시로 설정
@Entity(tableName = "photo_ticket_table")
data class DatabasePhotoTicket(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "photo_ticket_key")
    val key : String,
    val url: String,
    var frontText: String,
    var backText: String,
    @ColumnInfo(name = "photo_ticket_date")
    var date: String,
    @ColumnInfo(name = "photo_ticket_favorite")
    var favorite: Boolean = false,
    @ColumnInfo(name = "photo_ticket_user")
    var user : String,
    @ColumnInfo(name = "photo_ticket_album_name")
    val albumName: String,
    @ColumnInfo(name = "photo_ticket_album_id")
    val albumId: String,
    @ColumnInfo(name = "photo_ticket_album_key")
    val albumKey: String
) {

}


fun List<DatabasePhotoTicket>.asDomainModel(): List<PhotoTicket> {
    return map {
        PhotoTicket(
            id = it.key,
            url = it.url,
            frontText = it.frontText,
            backText = it.backText,
            date = it.date,
            favorite = it.favorite,
            albumInfo = listOf(it.albumId, it.albumKey, it.albumName)
        )
    }
}


fun DatabasePhotoTicket.asDomainModel(): PhotoTicket {
    return PhotoTicket(
        id = this.key,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        albumInfo = listOf(this.albumId, this.albumKey, this.albumName)
    )
}

fun DatabasePhotoTicket.asFirebaseModel(): FirebasePhotoTicket {
    return FirebasePhotoTicket(
        key = this.key,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        albumName= this.albumName,
        albumId = parseAlbumIdDomainToFirebase(this.albumId, this.albumKey),
        albumKey = this.albumKey
    )
}


//@Entity(tableName = "photo_ticket_table")
//data class PhotoTicket(
//    @PrimaryKey(autoGenerate = true) var id : Long =0L,
//    var photo: String,
//    var frontText : String,
//    var backText: String,
//    var date : String,
//    var favorite : Boolean = false
//) {
//
//}
