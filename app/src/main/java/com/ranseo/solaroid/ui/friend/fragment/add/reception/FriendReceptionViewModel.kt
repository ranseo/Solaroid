package com.ranseo.solaroid.ui.friend.fragment.add.reception

import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.Event
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.datasource.friend.FriendCommunicationDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.models.domain.asFirebaseModel
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchStatus
import com.ranseo.solaroid.repositery.friend.FriendCommunicateRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendReceptionViewModel(_myProfile: Profile) : ViewModel(){

    private val myProfile = _myProfile
    private val myFriendCode :Long = convertHexStringToLongFormat(myProfile.friendCode)

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendCommunicateRepositery = FriendCommunicateRepositery(
        fbAuth, fbDatabase,
        FriendCommunicationDataSource()
    )



    private val _friends =
        MutableLiveData<List<FriendListDataItem.ReceptionProfileDataItem>>(listOf())
    val friends: LiveData<List<FriendListDataItem.ReceptionProfileDataItem>>
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
        _clickAction.value = (friend.value != null && isClick.value != null)
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
            val listener : (friends:List<Friend>)->Unit = { friends ->
                viewModelScope.launch(Dispatchers.Default) {
                    val tmp = friends.distinct().map { FriendListDataItem.ReceptionProfileDataItem(
                        ReceptionFriend(it)
                    )}
                    withContext(Dispatchers.Main) {
                        _friends.value = tmp
                    }
                }
            }

            friendCommunicateRepositery.addValueListenerToReceptionRef(myFriendCode, listener)
        }
    }

    fun setValueMyFriendList(fri: Friend) {
        viewModelScope.launch {
            friendCommunicateRepositery.setValueMyFriendList(fri)
        }
    }

    fun setValueTmpFrientList(friendCode: Long) {
        viewModelScope.launch {
            friendCommunicateRepositery.setValueTmpList(friendCode, myProfile)
        }
    }

    fun setValueDispatchList(friendCode: Long, flag: DispatchStatus) {
        viewModelScope.launch {
            friendCommunicateRepositery.setValueFriendDispatch(friendCode, myProfile, flag, myFriendCode)
        }
    }

    fun deleteReceptionList() {
        viewModelScope.launch {
            try {

                friendCommunicateRepositery.deleteReceptionList(myFriendCode, friend!!.value!!.asFirebaseModel().friendCode)
            } catch (error: Exception) {
                Log.d(TAG, "deleteReceptionList() error : ${error}")
            }

        }
    }

    companion object {
        const val TAG = "프렌드_리셉션_뷰모델"
    }


}
