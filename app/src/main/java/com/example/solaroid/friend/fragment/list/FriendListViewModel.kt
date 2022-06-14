package com.example.solaroid.friend.fragment.list

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.domain.asDatabaseFriend
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.friend.adapter.FriendListDataItem
import com.example.solaroid.repositery.friend.FriendListRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendListViewModel(dataSource:DatabasePhotoTicketDao, _friendCode:Long) : ViewModel(),  MyFriendListDataSource.OnValueListener  {

    private val myFriendCode = _friendCode

    //firebase
    private val fbAuth : FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()

    //room
    private val database : DatabasePhotoTicketDao = dataSource

    //repositery
    private val friendListRepositery : FriendListRepositery = FriendListRepositery(fbAuth, fbDatabase, MyFriendListDataSource(this),  database)

    //data
    val friendList = friendListRepositery.friendList

    init {
        initRefreshFriendList()
    }

    fun initRefreshFriendList() {
        viewModelScope.launch {
            friendListRepositery.addListenerForMyFriendList()
            friendListRepositery.addTmpListValueEventListener(myFriendCode)
        }
    }

    fun setValueFriendListFromTmpList(friend: Friend) {
        viewModelScope.launch {
            friendListRepositery.setValueFriendListFromTmpList(friend)
        }
    }

    fun insertFriendToRoom(friend: Friend) {
        viewModelScope.launch {
            database.insert(friend.asDatabaseFriend())
        }
    }


    // MyFriendListDataSource.OnValueListener
    override fun onValueAdded(friend: Friend) {
        insertFriendToRoom(friend)
    }

    override fun onValueRemoved(friend: Friend) {

    }

    override fun onValueChanged(friend: Friend) {
        setValueFriendListFromTmpList(friend)
    }
}