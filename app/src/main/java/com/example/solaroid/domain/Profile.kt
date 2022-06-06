package com.example.solaroid.domain

import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.database.DatabaseFriend
import com.example.solaroid.firebase.FirebaseProfile

data class Profile(
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode: String
) {

}

fun Profile.asDatabaseFriend() : DatabaseFriend {
    return DatabaseFriend(
        user = id,
        nickname = nickname,
        profileImage =  profileImg,
        friendCode = friendCode
    )
}