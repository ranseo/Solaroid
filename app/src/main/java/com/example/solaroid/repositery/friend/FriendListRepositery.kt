package com.example.solaroid.repositery.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.asFirebaseModel
import com.example.solaroid.models.room.asDomainModel
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.firebase.FirebaseFriend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendListRepositery(
    private val roomDB: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val myFriendListDataSource: MyFriendListDataSource,
    roomDatabase: DatabasePhotoTicketDao

) {

    val user = fbAuth.currentUser!!.email ?: ""

    val friendList: LiveData<List<Friend>> =
        Transformations.map(roomDatabase.getAllFriends(user)) { list ->
            list.asDomainModel()
        }


    suspend fun addListenerForMyFriendList(insertRoom: (friend: Friend) -> Unit) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext
            val listener = myFriendListDataSource.getFriendListListener(insertRoom)
            fbDatabase.reference.child("myFriendList").child(user.uid).child("list")
                .addValueEventListener(listener)
        }
    }


    suspend fun addTmpListValueEventListener(myFriendCode: Long, setValue: (friend: Friend) -> Unit) {
        return withContext(Dispatchers.IO) {
            val listener = myFriendListDataSource.getTmpListener(setValue)
            fbDatabase.reference.child("tmpFriendList").child("$myFriendCode").child("list")
                .addValueEventListener(listener)
        }
    }

    suspend fun setValueFriendListFromTmpList(_friend: Friend) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext
            val ref = fbDatabase.reference.child("myFriendList").child(user.uid).child("list")
                .child("${convertHexStringToLongFormat(_friend.friendCode)}")

            val friend = FirebaseFriend(
                _friend.id,
                _friend.nickname,
                _friend.profileImg,
                convertHexStringToLongFormat(_friend.friendCode),
                "key"
            )

            ref.setValue(friend)
        }
    }

    suspend fun deleteTmpList(myFriendCode: Long, key: Long) {
        return withContext(Dispatchers.IO) {
            val ref =
                fbDatabase.reference.child("tmpFriendList").child("${myFriendCode}").child("list")
                    .child("$key")
            ref.removeValue()
        }
    }


    suspend fun deleteFriendFirebase(friendCode: Long) {
        return withContext(Dispatchers.IO) {
            try {
                val user = fbAuth.currentUser!!
                val ref = fbDatabase.reference.child("myFriendList").child(user.uid).child("list")
                    .child("$friendCode")
                ref.removeValue()
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }
    }

    suspend fun deleteFriendRoom(friendCode: String) {
        return withContext(Dispatchers.IO) {
            try {
                roomDB.deleteFriend(friendCode)
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }
    }


}