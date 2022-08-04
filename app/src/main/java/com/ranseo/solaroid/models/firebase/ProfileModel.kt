package com.ranseo.solaroid.models.firebase

import com.ranseo.solaroid.convertLongToHexStringFormat
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.room.DatabaseProfile

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

