package com.example.solaroid.friend.fragment.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.domain.Profile
import com.example.solaroid.domain.asDatabaseFriend
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.friend.adapter.FriendListDataItem
import com.example.solaroid.repositery.friend.FriendListRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
     * Room Database에 DatabaseFriend 객체 insert
     * */
    fun insertFriendToRoom(friend: Friend) {
        viewModelScope.launch {
            database.insert(friend.asDatabaseFriend())
            Log.i(TAG, "insertFriendToRoom")
        }
    }



    /**
     * 만약 내 친구요청을 상대가 받아줬다면
     * 해당 프렌드 객체는 TmpList에 저장된다.
     * 따라서 TmpList를 읽고 해당 프렌드 객체들을 다시 myFriendList에
     * setValue()하는 함수.
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
        const val TAG = "프렌드_리스트_뷰모델"
    }
}