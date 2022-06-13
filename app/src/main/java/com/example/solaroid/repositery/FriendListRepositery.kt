package com.example.solaroid.repositery

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.datasource.MyFriendListDataSource
import com.example.solaroid.domain.Profile
import com.example.solaroid.domain.asDatabaseFriend
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendListRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val myFriendListDataSource: MyFriendListDataSource,
    roomDatabase : DatabasePhotoTicketDao

)  {

    val friendList : LiveData<List<Profile>> = Transformations.map(roomDatabase.getAllFriends()){ list ->
        list.asDomainModel()
    }



    suspend fun addListenerForMyFriendList()  {
        withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser ?: return@withContext
            fbDatabase.reference.child("myFriendList").child(user.uid).child("list").addChildEventListener(myFriendListDataSource.friendListListener)
        }
    }







}