package com.example.solaroid.firebase

import com.example.solaroid.convertLongToHexStringFormat
import com.example.solaroid.domain.Profile

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

