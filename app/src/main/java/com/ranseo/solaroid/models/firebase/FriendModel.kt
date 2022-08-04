package com.ranseo.solaroid.firebase

import com.ranseo.solaroid.convertLongToHexStringFormat
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchStatus


data class FirebaseFriend(
    val id: String,
    val nickname: String,
    val profileImg: String,
    val friendCode: Long,
    var key: String
)

fun FirebaseFriend.asDomainModel(): Friend {
    return Friend(
        id,
        nickname,
        profileImg,
        convertLongToHexStringFormat(friendCode),
        key
    )
}

fun FirebaseFriend.asFirebaseDispatchFriend(status: String): FirebaseDispatchFriend {
    return FirebaseDispatchFriend(
        status,
        id,
        nickname,
        profileImg,
        friendCode
    )
}

data class FirebaseDispatchFriend(
    val flag: String,
    val id: String,
    val nickname: String,
    val profileImg: String,
    val friendCode: Long
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


