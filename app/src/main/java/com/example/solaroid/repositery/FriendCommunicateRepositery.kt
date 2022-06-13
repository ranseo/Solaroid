package com.example.solaroid.repositery

import android.util.Log
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.datasource.FriendCommunicationDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.firebase.FirebaseDispatchFriend
import com.example.solaroid.firebase.FirebaseFriend
import com.example.solaroid.friend.fragment.add.dispatch.DispatchStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendCommunicateRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val friendCommunicationDataSource: FriendCommunicationDataSource,
) {
    suspend fun addValueListenerToDisptachRef(friendCode: Long) {
        return withContext(Dispatchers.IO) {
            try {
                val listener = friendCommunicationDataSource.dispatchValueEventListener
                fbDatabase.reference.child("friendDispatch").child("${friendCode}").child("list")
                    .addValueEventListener(listener)
            } catch (e: Exception) {
                Log.i(TAG, "addValueListenerToDisptachRef error : ${e.message}")
            }
        }
    }

    suspend fun addValueListenerToReceptionRef(friendCode: Long) {
        return withContext(Dispatchers.IO) {
            try {
                val listener = friendCommunicationDataSource.receptionValueEventListener
                fbDatabase.reference.child("friendReception").child("${friendCode}").child("list")
                    .addValueEventListener(listener)
            } catch (e: Exception) {
                Log.i(TAG, "addValueListenerToReceptionRef error : ${e.message}")
            }
        }
    }

    suspend fun deleteReceptionList(friendCode: Long, key: String) {
        return withContext(Dispatchers.IO) {
            fbDatabase.reference.child("friendReception").child("${friendCode}").child("list")
                .child("${key}").removeValue()
        }

    }

    suspend fun setValueMyFriendList(_friend:Friend) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext

            val ref = fbDatabase.reference.child("myFriendList").child("${user.uid}").child("list").push()
            val key = ref.key ?: return@withContext
            val friend = FirebaseFriend(
                _friend.id,
                _friend.nickname,
                _friend.profileImg,
                convertHexStringToLongFormat(_friend.friendCode),
                key
            )

            ref.setValue(friend)

        }
    }

    suspend fun setValueTmpList(friendCode:Long, _friend:Friend) {
        return withContext(Dispatchers.IO) {

            val ref = fbDatabase.reference.child("tmpFriendList").child("${friendCode}").child("list").push()
            val key = ref.key ?: return@withContext
            val friend = FirebaseFriend(
                _friend.id,
                _friend.nickname,
                _friend.profileImg,
                convertHexStringToLongFormat(_friend.friendCode),
                key
            )

            ref.setValue(friend)

        }
    }

    suspend fun setValueFriendDispatch(friendCode: Long, myFriendCode:Long, _friend: Friend, flag:DispatchStatus) {
        return withContext(Dispatchers.IO) {

            val ref = fbDatabase.reference.child("friendDispatch").child("${friendCode}").child("list").child("${myFriendCode}")

            val key = ref.key ?: return@withContext
            val friend = FirebaseDispatchFriend(
                _friend.id,
                _friend.nickname,
                _friend.profileImg,
                convertHexStringToLongFormat(_friend.friendCode),
                key
            )

            ref.setValue(friend)

        }
    }


    companion object {
        const val TAG = "프렌드_커뮤니케이션_리포지터리"
    }
}