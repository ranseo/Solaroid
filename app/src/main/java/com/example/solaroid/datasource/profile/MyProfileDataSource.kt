package com.example.solaroid.datasource.profile

import android.util.Log
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseFriend
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow

class MyProfileDataSource() {

    suspend fun getMyProfileListener(
        insertRoomDb: suspend (profile: Profile) -> Unit
    )  {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(FirebaseProfile::class.java)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }


    }


    companion object {
        const private val TAG = "마이프로필_데이터소스"
    }

}
