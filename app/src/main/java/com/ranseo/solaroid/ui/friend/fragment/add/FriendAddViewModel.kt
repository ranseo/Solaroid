package com.ranseo.solaroid.ui.friend.fragment.add

import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.Event
import com.ranseo.solaroid.convertHexStringToLongFormat
import com.ranseo.solaroid.datasource.friend.FriendCommunicationDataSource
import com.ranseo.solaroid.datasource.friend.FriendSearchDataSource
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.domain.asFirebaseModel
import com.ranseo.solaroid.datasource.profile.MyProfileDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.repositery.friend.FriendAddRepositery
import com.ranseo.solaroid.repositery.friend.FriendCommunicateRepositery
import com.ranseo.solaroid.repositery.profile.ProfileRepostiery
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.ranseo.solaroid.ui.friend.activity.FriendActivityViewModel
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendAddViewModel(database: DatabasePhotoTicketDao) : ViewModel() {

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    //room
    private val dataSource = database

    //repositery
    private val friendAddRepositery = FriendAddRepositery(fbAuth, fbDatabase, FriendSearchDataSource())
    private val profileRepositery = ProfileRepostiery(fbAuth, fbDatabase, fbStorage, dataSource,
        MyProfileDataSource()
    )

    private val friendCommunicateRepositery = FriendCommunicateRepositery(
        fbAuth, fbDatabase,
        FriendCommunicationDataSource()
    )

    private val _searchUser = MutableLiveData<Profile?>(null)
    val searchUser: LiveData<Profile?>
        get() = _searchUser

    private val _receptionFriendSize = MutableLiveData<Int>()
    val receptionFriendSize : LiveData<Int>
        get() = _receptionFriendSize

    private val _dispatchFriendSize = MutableLiveData<Int>()
    val dispatchFriendSize : LiveData<Int>
        get() = _dispatchFriendSize


    val isSearchUser = Transformations.map(searchUser) { profile ->
        profile != null
    }

    val myProfile = profileRepositery.myProfile
    val myFriendCode = Transformations.map(myProfile) {
        convertHexStringToLongFormat(it.friendCode)
    }
    private var searchFriendCode: Long = -1

    private val _friendRequest = MutableLiveData<Event<Any?>>()
    val friendRequest: LiveData<Event<Any?>>
        get() = _friendRequest



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

    fun setSearchFriendCode(text: CharSequence) {
        Log.i(TAG, "setSearchFriendCode : ${text}")
        var code = text.toString()
        if (code.isNotEmpty()) {
            if (code.first() != '#') code = "#${code}"
            if (code.length >= 5)
                searchFriendCode = convertHexStringToLongFormat(code)
            else
                searchFriendCode = -1L
        }
        getSearchProfile()
    }


    private fun getSearchProfile() {
        viewModelScope.launch {
            Log.i(TAG, "start getSearchProfile()")
            if (searchFriendCode > -1L) {

                val listenerNull : (profile: Profile?) -> Unit = { profile ->
                    if(profile==myProfile.value || profile==null) setSearchUserNull()
                }

                val listenerSet : (profile:Profile) -> Unit = { profile ->
                    _searchUser.value = profile
                }

                friendAddRepositery.addSearchListener(searchFriendCode, listenerNull, listenerSet)
            } else {
                setSearchUserNull()
            }
        }
    }

    fun setSearchUserNull() {
        _searchUser.value = null
    }

    fun sendFriendRequest() {
        _friendRequest.value = Event(Unit)
    }

    fun setValueFriendReception() {
        viewModelScope.launch {
            friendAddRepositery.setValueToFriendReception(
                searchFriendCode,
                myProfile.value!!.asFirebaseModel()
            )
        }
    }


    fun setValueFriendDispatch() {
        viewModelScope.launch {
            friendAddRepositery.setValueToFriendDispatch(
                myProfile.value!!.asFirebaseModel(),
                searchFriendCode,
                searchUser.value!!.asFirebaseModel()
            )
        }
    }


    fun removeListener() {
        viewModelScope.launch {
            try {
                friendCommunicateRepositery.removeReceptionListener(myFriendCode.value!!)
                friendCommunicateRepositery.removeDispatchListener(myFriendCode.value!!)
            } catch (error:Exception){
                Log.e(FriendActivityViewModel.TAG,"removeListener() : ${error.message}")
            }

        }
    }


    companion object {
        const val TAG = "프렌드_애드_뷰모델"
    }

}