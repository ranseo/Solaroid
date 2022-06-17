package com.example.solaroid.friend.fragment.add.dispatch

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.datasource.friend.FriendCommunicationDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.friend.adapter.FriendListDataItem
import com.example.solaroid.friend.fragment.add.reception.ReceptionFriend
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

    private val _dispatchStatus = MutableLiveData<DispatchStatus>()
    val dispatchStatus: LiveData<DispatchStatus>
        get() = _dispatchStatus

    val statusMsg = Transformations.map(dispatchStatus) { status ->
        when (status) {

        }
    }


    init {
        refreshDispatchProfiles()
    }

    private fun refreshDispatchProfiles() {
        viewModelScope.launch {
            friendCommunicateRepositery.addValueListenerToDisptachRef(myFriendCode)
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
