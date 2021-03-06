package com.example.solaroid.ui.friend.fragment.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.domain.asDatabaseFriend
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.repositery.friend.FriendListRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class FriendListViewModel(dataSource:DatabasePhotoTicketDao) : ViewModel(),  MyFriendListDataSource.OnValueListener  {


    //firebase
    private val fbAuth : FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage : FirebaseStorage = FirebaseManager.getStorageInstance()
    //room
    private val database : DatabasePhotoTicketDao = dataSource

    //repositery
    private val friendListRepositery : FriendListRepositery = FriendListRepositery(fbAuth, fbDatabase, MyFriendListDataSource(this),  database)
    private val profileRepostiery : ProfileRepostiery = ProfileRepostiery(fbAuth,fbDatabase, fbStorage, dataSource,
        MyProfileDataSource()
    )

    //data
    val friendList = Transformations.map(friendListRepositery.friendList){
        it.map{ friend ->
            FriendListDataItem.NormalProfileDataItem(friend)
        }
    }

    val myProfile : LiveData<Profile> = profileRepostiery.myProfile



    fun initRefreshFriendList(friendCode:Long) {
        viewModelScope.launch {
            Log.i(TAG,"friendCode : ${friendCode}")
            friendListRepositery.addListenerForMyFriendList()
            friendListRepositery.addTmpListValueEventListener(friendCode)
        }
    }



    /**
     * Room Database??? DatabaseFriend ?????? insert
     * */
    fun insertFriendToRoom(friend: Friend) {
        viewModelScope.launch {
            val userEmail = fbAuth.currentUser!!.email ?: return@launch
            database.insert(friend.asDatabaseFriend(userEmail))
            Log.i(TAG, "insertFriendToRoom")
        }
    }



    /**
     * ?????? ??? ??????????????? ????????? ???????????????
     * ?????? ????????? ????????? TmpList??? ????????????.
     * ????????? TmpList??? ?????? ?????? ????????? ???????????? ?????? myFriendList???
     * setValue()?????? ??????.
     * */
    fun setValueFriendListFromTmpList(friend: Friend) {
        viewModelScope.launch {
            friendListRepositery.setValueFriendListFromTmpList(friend)
            Log.i(TAG, "setValueFriendListFromTmpList")
        }
    }

    fun deleteTmpList(friend:Friend) {
        viewModelScope.launch {
            friendListRepositery.deleteTmpList(convertHexStringToLongFormat(myProfile.value!!.friendCode), friend.key)
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
        deleteTmpList(friend)
    }

    companion object {
        const val TAG = "?????????_?????????_?????????"
    }
}