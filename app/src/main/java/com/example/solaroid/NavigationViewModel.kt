package com.example.solaroid

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.room.DatabaseProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class NavigationViewModel(database:DatabasePhotoTicketDao) : ViewModel(), ProfileRepostiery.ProfileRepositeryListener {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()


    private val dataSource = database

    val profileRepositery = ProfileRepostiery(fbAuth, fbDatabase, fbStorage, dataSource, this)

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


    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile>
        get() = _profile


    val emailId = Transformations.map(profile) {
        it.id
    }
    val nickName = Transformations.map(profile) {
        it.nickname
    }
    val url = Transformations.map(profile) {
        it.profileImg
    }
    val friendCode = Transformations.map(profile) {
        it.friendCode
    }

    init {
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

    companion object {
        const val TAG = "네비게이션뷰모델"
    }

    override fun insertRoomDatabase(profile: DatabaseProfile) {
    }


}