package com.example.solaroid.ui.friend.fragment.add.dispatch

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.datasource.friend.FriendCommunicationDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.repositery.friend.FriendCommunicateRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendDispatchViewModel(_myProfile: Profile) : ViewModel(){

    private val myProfile = _myProfile
    private val myFriendCode: Long = convertHexStringToLongFormat(myProfile.friendCode)

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery =
        FriendCommunicateRepositery(fbAuth, fbDatabase, FriendCommunicationDataSource())

    private val _friends =
        MutableLiveData<List<FriendListDataItem.DispatchProfileDataItem>>(listOf())
    val friends: LiveData<List<FriendListDataItem.DispatchProfileDataItem>>
        get() = _friends



    init {
        refreshDispatchProfiles()
    }

    private fun refreshDispatchProfiles() {
        viewModelScope.launch {
            val listener : (friends:List<DispatchFriend>)->Unit = { friends ->
                viewModelScope.launch(Dispatchers.Default) {
                    val tmp = friends.distinct().map {FriendListDataItem.DispatchProfileDataItem(it)}
                    withContext(Dispatchers.Main){
                        _friends.value = tmp
                    }
                }
            }

            friendCommunicateRepositery.addValueListenerToDisptachRef(myFriendCode, listener)
        }
    }

    fun deleteFriendInDispatchList(friend: DispatchFriend) {
        viewModelScope.launch {
            friendCommunicateRepositery.deleteFriendInDispatchList(myFriendCode, convertHexStringToLongFormat(friend.friendCode))
        }
    }

    companion object {
        const val TAG = "프렌드_디스패치_뷰모델"

    }
}
