package com.example.solaroid.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.example.solaroid.Event
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.ui.login.FirebaseAuthLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class SolaroidLoginViewModel(database: DatabasePhotoTicketDao) : ViewModel() {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private val dataSource = database

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    enum class LoginErrorType {
        EMAILTYPEERROR, PASSWORDERROR, ACCOUNTERROR, ISRIGHT, INVALID, EMPTY
    }

    val profileRepositery =
        ProfileRepostiery(fbAuth = fbAuth, fbDatabase = fbDatabase, fbStorage = fbStorage, dataSource,
            MyProfileDataSource()
        )

    private val _SavedLoginId = MutableLiveData<String>()
    val SavedLoginId: LiveData<String>
        get() = _SavedLoginId


    private val _isSaveId = MutableLiveData<Boolean>()
    val isSaveId: LiveData<Boolean>
        get() = _isSaveId

    fun setIsSaveId(b: Boolean) {
        _isSaveId.value = b
    }

    val myProfile = profileRepositery.myProfile


    val authenticationState = FirebaseAuthLiveData().map { user ->
        if (user != null) {
            val valid = user.isEmailVerified
            if (valid) {
                AuthenticationState.AUTHENTICATED
            } else AuthenticationState.INVALID_AUTHENTICATION
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    private val _loginBtn = MutableLiveData<Event<Any?>>()
    val loginBtn: LiveData<Event<Any?>>
        get() = _loginBtn


    private val _loginErrorType = MutableLiveData<LoginErrorType>()
    val loginErrorType: LiveData<LoginErrorType>
        get() = _loginErrorType

    val isSingUpAlert = Transformations.map(loginErrorType) { type ->
        when (type) {
            LoginErrorType.ISRIGHT, LoginErrorType.EMPTY -> false
            else -> true
        }
    }


    val alertText = Transformations.map(loginErrorType) { type ->
        val prefix = "???"
        prefix + when (type) {
            LoginErrorType.EMAILTYPEERROR -> "????????? ????????? ?????? ????????? ???????????????."
            LoginErrorType.PASSWORDERROR -> "????????? ??????????????? ???????????????."
            LoginErrorType.ACCOUNTERROR -> "????????? ?????? ??????????????? ???????????????."
            LoginErrorType.INVALID -> "?????? ????????? ?????? ???????????????."
            else -> ""
        }
    }


    //////////////////////

    private val _naviToNext = MutableLiveData<Event<Boolean>>()
    val naviToNext: LiveData<Event<Boolean>>
        get() = _naviToNext


    private val _naviToSignUp = MutableLiveData<Event<Any?>>()
    val naviToSignUp: LiveData<Event<Any?>>
        get() = _naviToSignUp


    fun navigateToSignUp() {
        _naviToSignUp.value = Event(Unit)
    }

    //////////////////////

    fun onLogin() {
        _loginBtn.value = Event(Unit)
    }


    fun setLoginErrorType(type: LoginErrorType) {
        _loginErrorType.value = type
    }


//    fun isProfileSet() {
//        viewModelScope.launch {
//            Log.i(TAG, "fun isProfileSet()")
//            val task = profileRepositery.isInitProfile()
//            if (task == null) {
//                Log.i(TAG, "fun isProfileSet() task null")
//                _naviToNext.value = Event(false)
//            }
//            else task.addOnCompleteListener {
//                if(it.isSuccessful) {
//                    Log.i(TAG, "fun isProfileSet() task success")
//                    if (it.result.exists()) _naviToNext.value = Event(true)
//                    else _naviToNext.value = Event(false)
//                } else {
//                    Log.i(TAG, "fun isProfileSet() task fail")
//                    _naviToNext.value = Event(false)
//                }
//            }
//        }
//    }

    fun isProfileSet() {
        if(myProfile.value ==null) _naviToNext.value = Event(false)
        else _naviToNext.value = Event(true)
    }

    fun isProfileAlready() {
        viewModelScope.launch {
            profileRepositery.isProfile()
                ?.addOnSuccessListener {
                    val data = it.value
                    if(data!=null) _naviToNext.value =  Event(true)
                    else _naviToNext.value = Event(false)
                }
        }
    }



    fun setSavedLoginId(id: String?) {
        if (id == null) return
        _SavedLoginId.value = id!!
    }

    companion object {
        const val TAG = "??????????????????"
    }


}