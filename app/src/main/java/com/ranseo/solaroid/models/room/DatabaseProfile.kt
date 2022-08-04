package com.ranseo.solaroid.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.firebase.FirebaseProfile

@Entity(tableName = "profile_table")
data class DatabaseProfile(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "profile_user")
    val id : String,
    var nickname: String,
    var profileImg : String,
    val friendCode : String
) {

}

fun DatabaseProfile.asDomainModel() : Profile {
    return Profile(
        id = id,
        nickname = nickname,
        profileImg = profileImg,
        friendCode = friendCode
    )
}

fun List<DatabaseProfile>.asDomainModel() : List<Profile> {
    return this.map {
        it.asDomainModel()
    }
}

fun DatabaseProfile.asFirebaseModel() : FirebaseProfile {
    return FirebaseProfile(
        id=id,
        nickname,
        profileImg,
        convertHexStringToLongFormat(friendCode)
    )
}