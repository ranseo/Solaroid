package com.example.solaroid.data.domain

import androidx.recyclerview.widget.DiffUtil
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.data.room.DatabaseFriend
import com.example.solaroid.firebase.FirebaseFriend


data class Friend(
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode: String,
    val key:String
) {

    companion object {
        val itemCallback = object :  DiffUtil.ItemCallback<Profile>(){
            override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem.friendCode == newItem.friendCode
            override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem == newItem
        }
    }

}

fun Friend.asDatabaseFriend() : DatabaseFriend {
    return DatabaseFriend(
        user = id,
        nickname = nickname,
        profileImage =  profileImg,
        friendCode = friendCode,
        key=key
    )
}

fun Friend.asFirebaseModel() : FirebaseFriend {
    return FirebaseFriend(
        id = id,
        nickname = nickname,
        profileImg =  profileImg,
        friendCode = convertHexStringToLongFormat(friendCode),
        key =key
    )
}