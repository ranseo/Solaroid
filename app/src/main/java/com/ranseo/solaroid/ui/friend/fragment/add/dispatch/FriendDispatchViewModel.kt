package com.ranseo.solaroid.ui.friend.fragment.add.dispatch

import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.datasource.friend.FriendCommunicationDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.repositery.friend.FriendCommunicateRepositery
import com.ranseo.solaroid.ui.friend.fragment.add.reception.FriendReceptionViewModel
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

    fun removeListener() {
        viewModelScope.launch {
            try {
                friendCommunicateRepositery.removeDispatchListener(myFriendCode)
            } catch (error: Exception) {
                Log.i(TAG, "removeListener() : ${error.message}")
            }
        }
    }
    companion object {
        const val TAG = "프렌드_디스패치_뷰모델"

    }
}
