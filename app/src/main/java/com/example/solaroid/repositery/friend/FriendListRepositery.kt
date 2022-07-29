package com.example.solaroid.repositery.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.asFirebaseModel
import com.example.solaroid.models.room.asDomainModel
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendListRepositery(
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


    suspend fun addListenerForMyFriendList() {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext
            fbDatabase.reference.child("myFriendList").child(user.uid).child("list")
                .addValueEventListener(myFriendListDataSource.friendListListener)
        }
    }

    suspend fun addTmpListValueEventListener(myFriendCode: Long) {
        return withContext(Dispatchers.IO) {
            fbDatabase.reference.child("tmpFriendList").child("${myFriendCode}").child("list")
                .addValueEventListener(myFriendListDataSource.tmpListListener)
        }
    }

    suspend fun setValueFriendListFromTmpList(_friend: Friend) {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext
            val ref = fbDatabase.reference.child("myFriendList").child(user.uid).child("list").child("${convertHexStringToLongFormat(_friend.friendCode)}")

            val friend = Friend(
                _friend.id,
                _friend.nickname,
                _friend.profileImg,
                _friend.friendCode,
                "key"
            ).asFirebaseModel()

            ref.setValue(friend)
        }
    }

    suspend fun deleteTmpList(myFriendCode:Long, key:Long) {
        return withContext(Dispatchers.IO) {
            val ref= fbDatabase.reference.child("tmpFriendList").child("${myFriendCode}").child("list").child("$key")
            ref.removeValue()
        }
    }


}