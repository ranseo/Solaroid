package com.example.solaroid.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class DatabaseProfile(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "profile_user")
    val user : String,
    var nickname: String?,
    var profileImage : String?,
    val friendCode : String
) {

}