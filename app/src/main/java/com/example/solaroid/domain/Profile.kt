package com.example.solaroid.domain

import androidx.recyclerview.widget.DiffUtil
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.database.DatabaseFriend
import com.example.solaroid.firebase.FirebaseProfile

data class Profile(
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode: String
) {

    companion object {
        val itemCallback = object :  DiffUtil.ItemCallback<Profile>(){
            override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem.friendCode == newItem.friendCode
            override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem == newItem
        }
    }

}

fun Profile.asDatabaseFriend() : DatabaseFriend {
    return DatabaseFriend(
        user = id,
        nickname = nickname,
        profileImage =  profileImg,
        friendCode = friendCode
    )
}

fun Profile.asFirebaseModel() : FirebaseProfile {
    return FirebaseProfile(
        id = id,
        nickname = nickname,
        profileImg =  profileImg,
        friendCode = convertHexStringToLongFormat(friendCode)
    )
}