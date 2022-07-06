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

class FriendDispatchViewModel(_myProfile: Profile) : ViewModel(),
    FriendCommunicationDataSource.OnDataListener {

    private val myProfile = _myProfile
    private val myFriendCode: Long = convertHexStringToLongFormat(myProfile.friendCode)

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery =
        FriendCommunicateRepositery(fbAuth, fbDatabase, FriendCommunicationDataSource(this))

    private val _friends =
        MutableLiveData<List<FriendListDataItem.DispatchProfileDataItem>>(listOf())
    val friends: LiveData<List<FriendListDataItem.DispatchProfileDataItem>>
        get() = _friends



    init {
        refreshDispatchProfiles()
    }

    private fun refreshDispatchProfiles() {
        viewModelScope.launch {
            friendCommunicateRepositery.addValueListenerToDisptachRef(myFriendCode)
        }
    }

    fun deleteFriendInDispatchList(friend: DispatchFriend) {
        viewModelScope.launch {
            friendCommunicateRepositery.deleteFriendInDispatchList(myFriendCode, convertHexStringToLongFormat(friend.friendCode))
        }
    }


    override fun onReceptionDataChanged(friend: List<Friend>) {

    }

    override fun onDispatchDataChanged(friend: List<DispatchFriend>) {
        viewModelScope.launch(Dispatchers.Default) {
            val tmp = friend.distinct().map { FriendListDataItem.DispatchProfileDataItem(it) }
            withContext(Dispatchers.Main) {
                _friends.value = tmp
            }
        }

        Log.i(TAG, "nDispatchDataChanged : ${friend}}")
    }

    companion object {
        const val TAG = "프렌드_디스패치_뷰모델"

    }
}
