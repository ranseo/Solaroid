package com.example.solaroid.database

import android.net.Uri
import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey

//변수 photo는 임시로 설정
@Entity(tableName = "photo_ticket_table")
data class PhotoTicket(
    @PrimaryKey(autoGenerate = true) var id : Long =0L,
    var photo: String,
    var frontText : String,
    var backText: String,
    var date : String,
    var favorite : Boolean = false
) {

}