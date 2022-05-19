package com.example.solaroid.firebase

import com.example.solaroid.domain.Profile

data class FirebaseProfile(
    val id: String,
    val nickname : String,
    val profileImg : String
)

fun FirebaseProfile.toDomainModel() : Profile {
    return Profile(
        id,
        nickname,
        profileImg
    )
}

