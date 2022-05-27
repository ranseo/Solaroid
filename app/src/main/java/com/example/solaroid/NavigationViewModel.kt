package com.example.solaroid

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.domain.Profile
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.firebase.toDomainModel
import com.example.solaroid.repositery.ProfileRepostiery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class NavigationViewModel : ViewModel() {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()


    val profileRepositery = ProfileRepostiery(fbAuth, fbDatabase, fbStorage)

    private val _naviToLoginAct = MutableLiveData<Event<Any?>>()
    val naviToLoginAct: LiveData<Event<Any?>>
        get() = _naviToLoginAct

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

    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            profileRepositery.getProfileInfo()?.addOnSuccessListener {
                try {
                    val profile = it.value as HashMap<*, *>



                    _profile.value = FirebaseProfile(
                        profile["id"] as String,
                        profile["nickname"] as String,
                        profile["profileImg"] as String
                    ).toDomainModel()
                } catch (error: Exception) {
                    Log.i(TAG, "profile value error : ${profile}")
                }
            }
        }
    }


    fun navigateToLoginAct() {
        _naviToLoginAct.value = Event(Unit)
    }

    companion object {
        const val TAG = "네비게이션뷰모델"
    }


}