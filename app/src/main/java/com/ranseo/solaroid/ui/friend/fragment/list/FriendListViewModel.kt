package com.ranseo.solaroid.ui.friend.fragment.list

import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.Event
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.domain.asDatabaseFriend
import com.ranseo.solaroid.datasource.friend.MyFriendListDataSource
import com.ranseo.solaroid.datasource.profile.MyProfileDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.repositery.friend.FriendListRepositery
import com.ranseo.solaroid.repositery.profile.ProfileRepostiery
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.ranseo.solaroid.ui.album.viewmodel.ClickTag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


//Friend 객체와 ClickTag enum class 객체의 Pair
typealias FT = Pair<Friend, ClickTag>

class FriendListViewModel(dataSource: DatabasePhotoTicketDao) : ViewModel() {

    //firebase
    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    //room
    private val database: DatabasePhotoTicketDao = dataSource

    //repositery
    private val friendListRepositery: FriendListRepositery =
        FriendListRepositery(database, fbAuth, fbDatabase, MyFriendListDataSource(), database)
    private val profileRepostiery: ProfileRepostiery = ProfileRepostiery(
        fbAuth, fbDatabase, fbStorage, dataSource,
        MyProfileDataSource()
    )

    //data
    val friendList = Transformations.map(friendListRepositery.friendList) {
        it.map { friend ->
            FriendListDataItem.NormalProfileDataItem(friend)
        }
    }

    val myProfile: LiveData<Profile> = profileRepostiery.myProfile


    private val _longClick = MutableLiveData<Event<FT?>>()
    val longClick: LiveData<Event<FT?>>
        get() = _longClick

    private val _currFriend = MutableLiveData<Event<FT?>>()
    val currFriend: LiveData<Event<FT?>>
        get() = _currFriend

    private val _tmpFriend = MutableLiveData<Event<Friend>>()
    val tmpFriend : LiveData<Event<Friend>>
        get() = _tmpFriend

    //

    fun initRefreshFriendList(friendCode: Long) {
        viewModelScope.launch {
            Log.i(TAG, "friendCode : ${friendCode}")
            val myFriendListener: (friend: Friend) -> Unit = { friend ->
                viewModelScope.launch {
                    val userEmail = fbAuth.currentUser!!.email ?: return@launch
                    database.insert(friend.asDatabaseFriend(userEmail))
                }
            }
            friendListRepositery.addListenerForMyFriendList(myFriendListener)


            val myTmpListener: (friend: Friend) -> Unit = { friend ->
                viewModelScope.launch {
                    friendListRepositery.setValueFriendListFromTmpList(friend)
                    _tmpFriend.value = Event(friend)
                }
            }
            friendListRepositery.addTmpListValueEventListener(friendCode, myTmpListener)
        }
    }

    fun deleteTmpList(friend: Friend) {
        viewModelScope.launch {
            friendListRepositery.deleteTmpList(
                convertHexStringToLongFormat(myProfile.value!!.friendCode),
                convertHexStringToLongFormat(friend.friendCode)
            )
        }
    }


    /**
     * 친구 삭제 시, firebase와 room 모두에서 friend data를 삭제한다.
     * */
    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            friendListRepositery.deleteFriendFirebase(convertHexStringToLongFormat(friend.friendCode))
            friendListRepositery.deleteFriendRoom(friend.friendCode)
        }
    }


    /**
     * friend list_item을 long Click시, longClick 프로퍼티 값을 할당
     * */
    fun onLongClick(friend: Friend, tag: ClickTag) {
        _longClick.value = Event(FT(friend, tag))
    }

    /**
     * 현재 friend를 설정
     * */
    fun setCurrFriend(ft: FT) {
        _currFriend.value = Event(ft)
    }


    companion object {
        const val TAG = "프렌드_리스트_뷰모델"
    }
}