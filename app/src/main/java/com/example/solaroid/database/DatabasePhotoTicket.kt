package com.example.solaroid.database

import android.net.Uri
import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicket

//변수 photo는 임시로 설정
@Entity(tableName = "photo_ticket_table")
data class DatabasePhotoTicket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    var frontText: String,
    var backText: String,
    var date: String,
    var favorite: Boolean = false,
    val firebaseKey: String
) {

}


fun List<DatabasePhotoTicket>.asDomainModel(): List<PhotoTicket> {
    return map {
        PhotoTicket(
            id = it.id,
            url = it.url,
            frontText = it.frontText,
            backText = it.backText,
            date = it.date,
            favorite = it.favorite
        )
    }
}


fun DatabasePhotoTicket.asDomainModel(): PhotoTicket {
    return PhotoTicket(
        id = this.id,
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite
    )
}

fun DatabasePhotoTicket.asFirebaseModel(): FirebasePhotoTicket {
    return FirebasePhotoTicket(
        url = this.url,
        frontText = this.frontText,
        backText = this.backText,
        date = this.date,
        favorite = this.favorite,
        key = this.firebaseKey
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
