package com.example.solaroid.models.firebase

import com.example.solaroid.convertLongToHexStringFormat
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.room.DatabaseProfile

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

fun List<FirebaseProfile>.asDomainModel() : List<Profile> {
    return this.map {
        it.asDomainModel()
    }
}

fun FirebaseProfile.asDatabaseModel() : DatabaseProfile {
    return DatabaseProfile(
        id = id,
        nickname,
        profileImg,
        convertLongToHexStringFormat(friendCode)
    )
}

fun List<FirebaseProfile>.asDatabaseModel() : List<DatabaseProfile> {
    return this.map {
        it.asDatabaseModel()
    }
}

