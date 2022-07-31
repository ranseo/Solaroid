package com.example.solaroid.ui.friend.fragment.add

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.datasource.friend.FriendSearchDataSource
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.domain.asFirebaseModel
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.asDomainModel
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.friend.FriendAddRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

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

    private val _searchUser = MutableLiveData<Profile?>(null)
    val searchUser: LiveData<Profile?>
        get() = _searchUser

    val searchProfile = Transformations.map(searchUser) {
        it?.let {

        }
    }

    val isSearchUser = Transformations.map(searchUser) { profile ->
        profile != null
    }

    val myProfile = profileRepositery.myProfile

    private var searchFriendCode: Long = -1

    private val _friendRequest = MutableLiveData<Event<Any?>>()
    val friendRequest: LiveData<Event<Any?>>
        get() = _friendRequest


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


    companion object {
        const val TAG = "프렌드_애드_뷰모델"
    }

}