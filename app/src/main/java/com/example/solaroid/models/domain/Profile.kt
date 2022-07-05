package com.example.solaroid.models.domain

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.room.DatabaseProfile
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    val id: String,
    val nickname : String,
    val profileImg : String,
    val friendCode: String,
) : Parcelable {

    companion object {
        val itemCallback = object :  DiffUtil.ItemCallback<Profile>(){
            override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem.friendCode == newItem.friendCode
            override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean = oldItem == newItem
        }
    }

}


fun Profile.asDatabaseModel() : DatabaseProfile {
    return DatabaseProfile(
        id,
        nickname,
        profileImg,
        friendCode
    )
}
fun Profile.asFirebaseModel() : FirebaseProfile {
    return FirebaseProfile(
        id = id,
        nickname = nickname,
        profileImg =  profileImg,
        friendCode = convertHexStringToLongFormat(friendCode),
    )
}

fun List<Profile>.asFirebaseModel() : List<FirebaseProfile> {
    return this.map {
        it.asFirebaseModel()
    }
}

fun Profile.asFriend(key:String) : Friend {
    return Friend(
        id=id,
        nickname=nickname,
        profileImg=profileImg,
        friendCode=friendCode,
        key=key
    )
}