package com.example.solaroid.friend.fragment.add

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.domain.Profile
import com.example.solaroid.domain.asFirebaseModel
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.repositery.friend.FriendAddRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class FriendAddViewModel : ViewModel() {

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    //repositery
    private val friendAddRepositery = FriendAddRepositery(fbAuth, fbDatabase)
    private val profileRepositery = ProfileRepostiery(fbAuth, fbDatabase, fbStorage)

    private val _searchUser = MutableLiveData<Profile?>(null)
    val searchUser: LiveData<Profile?>
        get() = _searchUser

    val searchProfile = Transformations.map(searchUser) {
        it?.let{

        }
    }

    val isSearchUser = Transformations.map(searchUser) { profile ->
        profile != null
    }

    private val _myProfile = MutableLiveData<Profile?>()
    val myProfile: LiveData<Profile?>
        get() = _myProfile

    val myFriendCode = Transformations.map(myProfile) {
        it?.let{
            convertHexStringToLongFormat(it.friendCode)
        }
    }

    private var searchFriendCode: Long = -1

    private val _friendRequest = MutableLiveData<Event<Any?>>()
    val friendRequest : LiveData<Event<Any?>>
        get() = _friendRequest



    init {
        refreshMyProfile()
    }

    private fun refreshMyProfile() {
        viewModelScope.launch {
            profileRepositery.getProfileInfo()?.addOnSuccessListener {
                try {
                    val profile = it.value as HashMap<*, *>

                    _myProfile.value = FirebaseProfile(
                        profile["id"] as String,
                        profile["nickname"] as String,
                        profile["profileImg"] as String,
                        profile["friendCode"] as Long
                    ).asDomainModel()
                } catch (error: Exception) {
                    _myProfile.value = null
                    Log.i(TAG, "profile value error : ${error.message}")
                }
            }
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
                val eventListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val hashMap = snapshot.value as HashMap<*, *>

                        val profile = FirebaseProfile(
                            hashMap["id"]!! as String,
                            hashMap["nickname"]!! as String,
                            hashMap["profileImg"]!! as String,
                            hashMap["friendCode"]!! as Long

                        ).asDomainModel()

                        if (profile == myProfile.value) setSearchUserNull()
                        else _searchUser.value = profile

                        Log.i(TAG, "task is Success, searchUser.value : ${searchUser.value}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        setSearchUserNull()
                        Log.i(TAG, "task is fail")
                    }
                }
                friendAddRepositery.addSearchListener(searchFriendCode,eventListener)
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
            friendAddRepositery.setValueToFriendReception(searchFriendCode, myProfile.value!!.asFirebaseModel())
        }
    }

    fun setValueFriendDispatch() {
        viewModelScope.launch {
            friendAddRepositery.setValueToFriendDispatch(myProfile.value!!.asFirebaseModel() ,searchFriendCode, searchUser.value!!.asFirebaseModel())
        }
    }


    companion object {
        const val TAG = "프렌드_애드_뷰모델"
    }
}