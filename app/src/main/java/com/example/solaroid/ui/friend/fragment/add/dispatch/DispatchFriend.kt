package com.example.solaroid.ui.friend.fragment.add.dispatch


enum class DispatchStatus(val status: String) {
    UNKNOWN(status = "UNKNOWN"), DECLINE("DECLINE"), ACCEPT("ACCEPT");

    companion object {
        fun convertStringToStatus(status: String): DispatchStatus {
            return when (status) {
                "UNKNOWN" -> UNKNOWN
                "DECLINE" -> DECLINE
                "ACCEPT" -> ACCEPT
                else -> throw IllegalArgumentException("UNDEFINED_STATUS")
            }
        }
    }
}

data class DispatchFriend(
    val flag: DispatchStatus = DispatchStatus.UNKNOWN,
    val id: String,
    val nickname: String,
    val profileImg: String,
    val friendCode: String,
) {
}


//data class DispatchFriend(
//    var flag : DispatchStatus = DispatchStatus.UNKNOWN,
//    val friend: Friend
//) {
//}