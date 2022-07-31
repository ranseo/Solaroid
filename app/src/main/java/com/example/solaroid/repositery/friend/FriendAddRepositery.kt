package com.example.solaroid.repositery.friend

import com.example.solaroid.datasource.friend.FriendSearchDataSource
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.firebase.FirebaseDispatchFriend
import com.example.solaroid.firebase.FirebaseFriend
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.ui.friend.fragment.add.dispatch.DispatchStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendAddRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val friendSearchDataSource : FriendSearchDataSource
) {

    suspend fun addSearchListener(friendCode: Long, listenerNull:(profile: Profile?)->Unit, listenerSet:(profile:Profile)->Unit) {
        return withContext(Dispatchers.IO) {
            val listener = friendSearchDataSource.getValueEventListener(listenerNull, listenerSet)
            fbDatabase.reference.child("allUsers").child("$friendCode").addListenerForSingleValueEvent(listener)
        }
    }

    suspend fun setValueToFriendReception(friendCode: Long, myProfile: FirebaseProfile) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser
            if(user!=null) {
                val ref = fbDatabase.reference.child("friendReception").child("${friendCode}").child("list").child("${myProfile.friendCode}")
                val friend = FirebaseFriend(
                    myProfile.id,
                    myProfile.nickname,
                    myProfile.profileImg,
                    myProfile.friendCode,
                    ""
                )

                ref.setValue(friend)
            }
        }
    }

    suspend fun setValueToFriendDispatch(myProfile: FirebaseProfile, friendCode: Long, friendProfile: FirebaseProfile) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser
            if(user!=null) {
                val ref = fbDatabase.reference.child("friendDispatch").child("${myProfile.friendCode}").child("list").child("${friendCode}")

                val friend = FirebaseDispatchFriend(
                    DispatchStatus.UNKNOWN.status,
                    friendProfile.id,
                    friendProfile.nickname,
                    friendProfile.profileImg,
                    friendProfile.friendCode
                )

                ref.setValue(friend)
            }
        }
    }



    companion object {
        const val TAG = "프렌드 애드 리포지터리"
    }

}