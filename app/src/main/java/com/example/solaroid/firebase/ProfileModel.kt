package com.example.solaroid.firebase

import com.example.solaroid.convertLongToDecimalFormat
import com.example.solaroid.domain.Profile

data class FirebaseProfile(
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode : Long
)

fun FirebaseProfile.toDomainModel() : Profile {
    return Profile(
        id,
        nickname,
        profileImg,
        convertLongToDecimalFormat(friendCode)
    )
}

