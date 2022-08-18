package com.ranseo.solaroid.ui.friend.activity

import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.datasource.friend.FriendCommunicationDataSource
import com.ranseo.solaroid.datasource.friend.FriendSearchDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.repositery.friend.FriendAddRepositery
import com.ranseo.solaroid.repositery.friend.FriendCommunicateRepositery
import com.ranseo.solaroid.ui.friend.adapter.FriendListDataItem
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import com.ranseo.solaroid.ui.friend.fragment.add.reception.ReceptionFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendActivityViewModel : ViewModel() {
    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()


    //
    private val _receptionFriendSize = MutableLiveData<Int>()
    val receptionFriendSize : LiveData<Int>
        get() = _receptionFriendSize

    private val _dispatchFriendSize = MutableLiveData<Int>()
    val dispatchFriendSize : LiveData<Int>
        get() = _dispatchFriendSize

    private val _totalFriendSize = MediatorLiveData<Int>()
    val totalFriendSize : LiveData<Int>
        get() = _totalFriendSize

    private val _myFriendCode = MutableLiveData<Long>()
    val myFriendCode : LiveData<Long>
        get() = _myFriendCode


    private fun setTotalFriendSize(receptionSize: LiveData<Int>,dispatchSize:LiveData<Int>) {
        _totalFriendSize.value = (receptionSize.value ?: 0) + (dispatchSize.value ?: 0)
    }


    private val friendCommunicateRepositery = FriendCommunicateRepositery(
        fbAuth, fbDatabase,
        FriendCommunicationDataSource()
    )

    init {
        _totalFriendSize.apply {
            addSource(receptionFriendSize) {
                setTotalFriendSize(receptionFriendSize, dispatchFriendSize)
            }
            addSource(dispatchFriendSize) {
                setTotalFriendSize(receptionFriendSize, dispatchFriendSize)
            }
        }
    }


    fun refreshReceptionFriendSize(myFriendCode:Long) {
        viewModelScope.launch {
            val listener : (friends:List<Friend>)->Unit = { friends ->
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        _receptionFriendSize.value = friends.size
                    }
                }
            }
            friendCommunicateRepositery.addValueListenerToReceptionRef(myFriendCode, listener)
        }
    }

    fun refreshDispatchFriendSize(myFriendCode:Long) {
        viewModelScope.launch {
            val listener : (friends:List<DispatchFriend>)->Unit = { friends ->
                viewModelScope.launch() {
                    withContext(Dispatchers.Main){
                        _dispatchFriendSize.value = friends.size
                    }
                }
            }

            friendCommunicateRepositery.addValueListenerToDisptachRef(myFriendCode, listener)
        }
    }

    fun setMyFriendCode(friendCode:Long) {
        _myFriendCode.value = friendCode
    }


    fun removeListener() {
        viewModelScope.launch {
            try {
                friendCommunicateRepositery.removeReceptionListener(myFriendCode.value!!)
                friendCommunicateRepositery.removeDispatchListener(myFriendCode.value!!)
            } catch (error:Exception){
                Log.e(TAG,"removeListener() : ${error.message}")
            }

        }
    }

    companion object {
        const val TAG = "프랜드 액티비티"
    }

}