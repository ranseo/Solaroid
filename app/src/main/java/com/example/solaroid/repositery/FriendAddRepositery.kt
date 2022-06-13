package com.example.solaroid.repositery

import android.util.Log
import com.example.solaroid.datasource.FriendSearchDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseFriend
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.friend.fragment.add.dispatch.DispatchFriend
import com.example.solaroid.friend.fragment.add.dispatch.DispatchStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendAddRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase
) {

    suspend fun addSearchListener(friendCode: Long, listener:ValueEventListener) {
        return withContext(Dispatchers.IO) {
            fbDatabase.reference.child("allUsers").child("${friendCode}").addListenerForSingleValueEvent(listener)
        }
    }

    suspend fun setValueToFriendReception(friendCode: Long, myProfile: FirebaseProfile) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser
            if(user!=null) {
                val ref = fbDatabase.reference.child("friendReception").child("${friendCode}").child("list").push()
                val key = ref.key ?: return@withContext
                val friend = FirebaseFriend(
                    myProfile.id,
                    myProfile.nickname,
                    myProfile.profileImg,
                    myProfile.friendCode,
                    key
                )

                ref.setValue(friend)
            }
        }
    }

    suspend fun setValueToFriendDispatch(myFriendCode:Long, friendCode: Long, myProfile: FirebaseProfile) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser
            if(user!=null) {
                val ref = fbDatabase.reference.child("friendDispatch").child("${myFriendCode}").child("list").child("${friendCode}")
                val key = ref.key ?: return@withContext


                val friend = FirebaseFriend(
                    myProfile.id,
                    myProfile.nickname,
                    myProfile.profileImg,
                    myProfile.friendCode,
                    key
                )



                ref.setValue(friend)
            }
        }
    }



    companion object {
        const val TAG = "프렌드 애드 리포지터리"
    }

}