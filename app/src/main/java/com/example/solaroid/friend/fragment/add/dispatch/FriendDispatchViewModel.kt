package com.example.solaroid.friend.fragment.add.dispatch

import androidx.lifecycle.*
import com.example.solaroid.datasource.friend.FriendCommunicationDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.friend.adapter.FriendListDataItem
import com.example.solaroid.repositery.friend.FriendCommunicateRepositery
import kotlinx.coroutines.launch

class FriendDispatchViewModel(_friendCode:Long) : ViewModel(), FriendCommunicationDataSource.OnDataListener {

    private val friendCode = _friendCode

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery = FriendCommunicateRepositery(fbAuth, fbDatabase, FriendCommunicationDataSource(this))

    private val _friends = MutableLiveData<List<DispatchFriend>>(listOf())
    val friends: LiveData<List<DispatchFriend>>
        get() = _friends

    val profilesDistinct = Transformations.map(friends) {
        it.distinct()
    }


    init {
        refreshDispatchProfiles()
    }

    private fun refreshDispatchProfiles() {
        viewModelScope.launch {
            friendCommunicateRepositery.addValueListenerToDisptachRef(friendCode)
        }
    }




    override fun onReceptionDataChanged(friend: List<Friend>) {

    }

    override fun onDispatchDataChanged(friend: List<DispatchFriend>) {
        _friends.value = friend
    }

    companion object {
        const val TAG = "프렌드_디스패치_뷰모델"
    }
}
