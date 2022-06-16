package com.example.solaroid.firebase

import com.example.solaroid.convertLongToHexStringFormat
import com.example.solaroid.domain.Profile
import com.example.solaroid.room.DatabaseProfile

data class FirebaseProfile(
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode : Long
)

fun FirebaseProfile.asDomainModel() : Profile {
    return Profile(
        id,
        nickname,
        profileImg,
        convertLongToHexStringFormat(friendCode)
    )
}

fun FirebaseProfile.asDatabaseModel() : DatabaseProfile {
    return DatabaseProfile(
        id = id,
        nickname,
        profileImg,
        convertLongToHexStringFormat(friendCode)
    )
}

