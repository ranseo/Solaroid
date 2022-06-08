package com.example.solaroid.friend.fragment.add

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.repositery.FriendAddRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendAddViewModel : ViewModel() {

    //firebase
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()

    //repositery
    private val friendAddRepositery = FriendAddRepositery(fbAuth, fbDatabase)

    private val _searchUser = MutableLiveData<Profile?>(null)
    val searchUser : LiveData<Profile?>
        get() = _searchUser

    val isSearchUser = Transformations.map(searchUser) { profile ->
        profile != null
    }

    private var searchFriendCode : Long = -1



    init {

    }

    fun setSearchFriendCode(text:CharSequence) {
        Log.i(TAG,"setSearchFriendCode : ${text}")
        var code = text.toString()
        if(code.isNotEmpty()) {
            if(code.first() != '#') code = "#${code}"
            if(code.length >= 5)
                searchFriendCode = convertHexStringToLongFormat(code)
            else
                searchFriendCode = -1L
        }
        getSearchProfile()

    }

    fun getSearchProfile() {
        viewModelScope.launch {
            Log.i(TAG,"getSearchProfile()")
            if(searchFriendCode!=null && searchFriendCode > -1L)
                friendAddRepositery.getTask(searchFriendCode!!).addOnCompleteListener {
                    if(it.isSuccessful) {
                        val hashMap = it.result.value as HashMap<*,*>? ?: return@addOnCompleteListener

                        _searchUser.value = FirebaseProfile(
                            hashMap["id"]!! as String,
                            hashMap["nickname"]!! as String,
                            hashMap["profileImg"]!! as String,
                            hashMap["friendCode"]!! as Long
                        ).asDomainModel()

                        Log.i(TAG,"task is Success, searchUser.value : ${searchUser.value}")
                    } else {
                        setSearchUserNull()
                        Log.i(TAG,"task is fail")
                    }
                }
            else setSearchUserNull()
        }
    }

    fun setSearchUserNull(){
        _searchUser.value = null

    }


    companion object {
        const val TAG = "프렌드_애드_뷰모델"
    }
}