package com.example.solaroid.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.domain.Profile

@Entity(tableName = "friend_table")
data class DatabaseFriend(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "friend_code")
    val friendCode : String,
    val user : String,
    @ColumnInfo(name = "friend_nickname")
    var nickname: String,
    var profileImage : String
) {

}

fun List<DatabaseFriend>.asDomainModel() : List<Profile> {
    return this.map{
        Profile(
        it.user,
        it.nickname,
        it.profileImage,
        it.friendCode
    )}
}