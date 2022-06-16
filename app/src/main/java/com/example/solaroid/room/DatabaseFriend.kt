package com.example.solaroid.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.domain.Friend

@Entity(tableName = "friend_table")
data class DatabaseFriend(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "friend_code")
    val friendCode: String,
    val user: String,
    @ColumnInfo(name = "friend_nickname")
    var nickname: String,
    var profileImage: String,
    val key: String
) {

}

fun List<DatabaseFriend>.asDomainModel(): List<Friend> {
    return this.map {
        Friend(
            it.user,
            it.nickname,
            it.profileImage,
            it.friendCode,
            it.key
        )
    }
}