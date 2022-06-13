package com.example.solaroid.firebase

import com.example.solaroid.convertLongToHexStringFormat
import com.example.solaroid.domain.Friend
import com.example.solaroid.friend.fragment.add.dispatch.DispatchFriend
import com.example.solaroid.friend.fragment.add.dispatch.DispatchStatus


data class FirebaseFriend (
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode : Long,
    var key : String
)

fun FirebaseFriend.asDomainModel() : Friend {
    return Friend(
        id,
        nickname,
        profileImg,
        convertLongToHexStringFormat(friendCode),
        key
    )
}

data class FirebaseDispatchFriend(
    val flag : String,
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode: Long,
) {
}

fun FirebaseDispatchFriend.asDomainModel(): DispatchFriend {
    return DispatchFriend(
        DispatchStatus.convertStringToStatus(flag),
        id,
        nickname,
        profileImg,
        convertLongToHexStringFormat(friendCode),
    )
}
