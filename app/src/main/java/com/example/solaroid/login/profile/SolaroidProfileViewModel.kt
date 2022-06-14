package com.example.solaroid.login.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebaseProfile
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.repositery.user.UsersRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolaroidProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    val profileRepositery = ProfileRepostiery(fbAuth, fbDatabase, fbStorage)
    val usersRepositery = UsersRepositery(fbAuth, fbDatabase, fbStorage)

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

    var firebaseProfile: FirebaseProfile? = null

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

    var allUserNum: Long = 0L


    fun getFriendCode() {
        viewModelScope.launch {
            usersRepositery.getAllUserNum()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    try {
                        allUserNum = it.result.value as Long
                        Log.i(TAG, "getFriendCode : ${allUserNum}")

                    } catch (error: Exception) {
                        Log.i(TAG, "getFriendCode error : ${error.message}")
                    }
                } else {
                    Log.i(TAG, "getFriendCode error : ${it.exception?.message}")
                }
            }

        }
    }


    private val _naviToMain = MutableLiveData<Event<Any?>>()
    val naviToMain: LiveData<Event<Any?>>
        get() = _naviToMain

    fun navigateToMain() {
        _naviToMain.value = Event(Unit)
    }

    private val _naviToLogin = MutableLiveData<Event<Any?>>()
    val naviToLogin: LiveData<Event<Any?>>
        get() = _naviToLogin

    fun navigateToLogin() {
        _naviToLogin.value = Event(Unit)
    }

    init {
        getFriendCode()
    }


    fun onNicknameEditTextChanged(str: CharSequence) {
        _nickname.value = str.toString()
    }

    fun setProfileUrl(uri: Uri) {
        _profileUrl.value = uri.toString()
    }


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

    fun insertRefreshUpdateInsert() {
        viewModelScope.launch {
            insertProfileFirebase()
            refreshProfile()
            updateAllUsersNum()
            insertUserList()
            navigateToMain()
        }
    }


    suspend fun insertProfileFirebase(): Unit {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val profile = FirebaseProfile(
                user.email!!,
                nickname = nickname.value!!,
                profileImg = profileUrl.value!!,
                friendCode = (allUserNum + 1)
            )

            profileRepositery.insertProfileInfo(profile, getApplication())
            delay(500)
        }
    }

    suspend fun refreshProfile(): Unit {
        return withContext(Dispatchers.IO) {
            profileRepositery.getProfileInfo()?.addOnSuccessListener {
                try {
                    val profile = it.value as HashMap<*, *>

                    firebaseProfile = FirebaseProfile(
                        profile["id"] as String,
                        profile["nickname"] as String,
                        profile["profileImg"] as String,
                        profile["friendCode"] as Long
                    )
                } catch (error: Exception) {
                    Log.i(TAG, "profile value error : ${error.message}")
                }
            }
        }
    }

    suspend fun updateAllUsersNum(): Unit {
        withContext(Dispatchers.IO) {
            usersRepositery.updateAllUserNum(allUserNum + 1L)
        }
    }

    suspend fun insertUserList() {
        withContext(Dispatchers.IO) {
            delay(1000)
            usersRepositery.insertUsersList(firebaseProfile!!)
        }
    }

    companion object {
        const val TAG = "프로필 뷰모델"
    }


}