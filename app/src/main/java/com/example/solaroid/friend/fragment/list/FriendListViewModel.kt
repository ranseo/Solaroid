package com.example.solaroid.friend.fragment.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.datasource.MyFriendListDataSource
import com.example.solaroid.domain.Profile
import com.example.solaroid.domain.asDatabaseFriend
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.repositery.FriendListRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendListViewModel(dataSource:DatabasePhotoTicketDao) : ViewModel(),  MyFriendListDataSource.OnValueListener  {

    //firebase
    private val fbAuth : FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()

    //room
    private val database : DatabasePhotoTicketDao = dataSource

    //repositery
    private val friendListRepositery : FriendListRepositery = FriendListRepositery(fbAuth, fbDatabase, MyFriendListDataSource(this),  database)

    //data
    val friendList : LiveData<List<Profile>> = friendListRepositery.friendList

    init {
        initRefreshFriendList()
    }

    fun initRefreshFriendList() {
        viewModelScope.launch {
            friendListRepositery.addListenerForMyFriendList()
        }
    }


    // MyFriendListDataSource.OnValueListener
    override fun onValueAdded(profile: FirebaseProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            database.insert(profile.asDomainModel().asDatabaseFriend())
        }
    }

    override fun onValueRemoved(profile: FirebaseProfile) {

    }
}