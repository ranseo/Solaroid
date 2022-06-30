package com.example.solaroid

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.domain.Profile
import com.example.solaroid.domain.asDatabaseModel
import com.example.solaroid.domain.asFirebaseModel
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.room.DatabaseProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class NavigationViewModel(database: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()


    private val dataSource = database

    val profileRepositery =
        ProfileRepostiery(fbAuth, fbDatabase, fbStorage, dataSource, MyProfileDataSource())


    val myProfile = profileRepositery.myProfile

    private val _naviToLoginAct = MutableLiveData<Event<Any?>>()
    val naviToLoginAct: LiveData<Event<Any?>>
        get() = _naviToLoginAct

    private val _naviToHomeAct = MutableLiveData<Event<Any?>>()
    val naviToHomeAct: LiveData<Event<Any?>>
        get() = _naviToHomeAct


    private val _naviToFriendAct = MutableLiveData<Event<Any?>>()
    val naviToFriendAct: LiveData<Event<Any?>>
        get() = _naviToFriendAct

    private val _naviToAlbumAct = MutableLiveData<Event<Any?>>()
    val naviToAlbumAct: LiveData<Event<Any?>>
        get() = _naviToAlbumAct


    val emailId = Transformations.map(myProfile) {
        it.id
    }
    val nickName = Transformations.map(myProfile) {
        it.nickname
    }
    val url = Transformations.map(myProfile) {
        it.profileImg
    }
    val friendCode = Transformations.map(myProfile) {
        it.friendCode
    }

    init {

    }


    fun insertProfileRoomDatabase() {
        viewModelScope.launch {
            Log.i(TAG, "profileRepositery.getProfileInfo : coroutine : ${this}")
            profileRepositery.getProfileInfo() { profile ->
                Log.i(TAG,"profileRepositery : ${this}")
                viewModelScope.launch {
                    Log.i(TAG,"profileRepositery.getProfileInfo() : ${this}")
                    profileRepositery.insertRoomDatabase(profile)
                }

            }
        }
    }


    fun navigateToLoginAct() {
        _naviToLoginAct.value = Event(Unit)
    }

    fun navigateToHomeAct() {
        _naviToHomeAct.value = Event(Unit)
    }

    fun navigateToFriendAct() {
        _naviToFriendAct.value = Event(Unit)
    }

    fun navigateToAlbumAct() {
        _naviToAlbumAct.value = Event(Unit)
    }

    companion object {
        const val TAG = "네비게이션뷰모델"
    }


}