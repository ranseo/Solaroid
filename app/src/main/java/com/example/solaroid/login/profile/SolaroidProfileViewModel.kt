package com.example.solaroid.login.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.repositery.ProfileRepostiery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class SolaroidProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    val profileRepositery = ProfileRepostiery(fbAuth, fbDatabase, fbStorage)

    enum class ProfileErrorType {
        IMAGEERROR, NICKNAMEERROR, ISRIGHT, EMPTY
    }

    private val _nickname = MutableLiveData<String>("")
    val nickname: LiveData<String>
        get() = _nickname


    val nicknameLen = Transformations.map(nickname) {
        val len = if (it.isNullOrEmpty()) 0
        else it.length

        "${len}/12"
    }

    private val _profileUrl = MutableLiveData<String?>()
    val profileUrl: LiveData<String?>
        get() = _profileUrl

    val isSetProfile = Transformations.map(profileUrl) {
        it != null
    }

    private val _profileType = MutableLiveData<ProfileErrorType>()
    val profileType: LiveData<ProfileErrorType>
        get() = _profileType

    fun setProfileType(type: ProfileErrorType) {
        _profileType.value = type
    }

    val isAlamVisible = Transformations.map(profileType) { type ->
        when (type) {
            ProfileErrorType.ISRIGHT, ProfileErrorType.EMPTY -> false
            else -> true
        }
    }

    val alertMessage = Transformations.map(profileType) { type ->
        "※" + when (type) {
            ProfileErrorType.NICKNAMEERROR -> "별명을 입력해주세요"
            ProfileErrorType.IMAGEERROR -> "프로필 사진을 설정해주세요"
            else -> ""
        }
    }


    fun onNicknameEditTextChanged(str: CharSequence) {
        _nickname.value = str.toString()
    }

    fun setProfileUrl(uri: Uri) {
        _profileUrl.value = uri.toString()
    }

    init {
        viewModelScope.launch {
            val isInit = profileRepositery.isInitProfile()
            if (isInit) navigateToMain() else doneNavigateToMain()
        }
    }

    ///////////////

    private val _naviToMain = MutableLiveData<Boolean>()
    val naviToMain: LiveData<Boolean>
        get() = _naviToMain

    fun navigateToMain() {
        _naviToMain.value = true
    }

    fun doneNavigateToMain() {
        _naviToMain.value = false
    }

    ///////////////

    private val _addImage = MutableLiveData<Event<Any?>>()
    val addImage: LiveData<Event<Any?>>
        get() = _addImage


    fun onAddBtn() {
        _addImage.value = Event(Unit)
    }

    private val _setProfile = MutableLiveData<Event<Any?>>()
    val setProfile: LiveData<Event<Any?>>
        get() = _setProfile

    fun onSetProfile() {
        _setProfile.value = Event(Unit)
    }

    ///////////////


    fun insertProfileFirebase() {
        viewModelScope.launch {

            val user = fbAuth.currentUser!!
            val profile = FirebaseProfile(
                user.email!!,
                nickname = nickname.value!!,
                profileImg = profileUrl.value!!
            )

            profileRepositery.insertProfileInfo(profile, getApplication())
        }
    }


}