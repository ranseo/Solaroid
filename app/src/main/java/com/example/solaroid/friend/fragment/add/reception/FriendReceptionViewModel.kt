package com.example.solaroid.friend.fragment.add.reception

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.datasource.FriendCommunicationDataSource
import com.example.solaroid.domain.Friend
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.FriendCommunicateRepositery
import kotlinx.coroutines.launch

class FriendReceptionViewModel(_friendCode: Long) : ViewModel(),
    FriendCommunicationDataSource.OnDataListener {

    private val friendCode = _friendCode

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery = FriendCommunicateRepositery(
        fbAuth, fbDatabase,
        FriendCommunicationDataSource(this)
    )

    private val _friends = MutableLiveData<List<Friend>>(listOf())
    val friends: LiveData<List<Friend>>
        get() = _friends

    private val _friend = MutableLiveData<Friend?>()
    val friend: LiveData<Friend?>
        get() = _friend

    private val _isClick = MutableLiveData<Event<Boolean>>()
    val isClick: LiveData<Event<Boolean>>
        get() = _isClick

    private val _clickAction = MediatorLiveData<Boolean>()
    val clickAction: LiveData<Boolean>
        get() = _clickAction

    private fun checkClickAction(friend: LiveData<Friend?>, isClick: LiveData<Event<Boolean>>) {
        _clickAction.value = (friend.value != null && isClick.value!!.peekContent())
    }

    val profilesDistinct = Transformations.map(friends) {
        it.distinct().map { ReceptionFriend(it) }
    }


    init {
        refreshReceptionProfiles()

        with(_clickAction) {
            addSource(friend) {
                checkClickAction(friend, isClick)
            }
            addSource(isClick) {
                checkClickAction(friend, isClick)
            }
        }
    }

    fun onAccept(fri: Friend) {
        _friend.value = fri
        _isClick.value = Event(true)
    }

    fun onDecline(fri: Friend) {
        _friend.value = fri
        _isClick.value = Event(false)
    }

    private fun refreshReceptionProfiles() {
        viewModelScope.launch {
            friendCommunicateRepositery.addValueListenerToReceptionRef(friendCode)
        }
    }

    fun setValueMyFriendList(fri:Friend) {
        viewModelScope.launch {
            friendCommunicateRepositery.setValueMyFriendList(fri)
        }
    }

    fun setValueTmpFrientList(friendCode:Long, fri:Friend) {
        viewModelScope.launch {
            friendCommunicateRepositery.setValueTmpList(friendCode,fri)
        }
    }

    fun deleteReceptionList() {
        viewModelScope.launch {
            try {
                friendCommunicateRepositery.deleteReceptionList(friendCode, friend.value!!.key)
            } catch (error:Exception) {
                Log.d(TAG,"deleteReceptionList() error : ${error}")
            }

        }
    }

    companion object {
        const val TAG = "프렌드_리셉션_뷰모델"
    }

    override fun onDataChanged(friend: List<Friend>) {
        _friends.value = friend
    }


}
