package com.example.solaroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class DatabaseProfile(
    @PrimaryKey(autoGenerate = false)
    val user : String,
    var nickname: String?,
    var profileImage : String?
) {

}