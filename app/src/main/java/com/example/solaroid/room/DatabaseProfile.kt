package com.example.solaroid.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseProfile

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

fun DatabaseProfile.asFirebaseModel() : FirebaseProfile {
    return FirebaseProfile(
        id=id,
        nickname,
        profileImg,
        convertHexStringToLongFormat(friendCode)
    )
}