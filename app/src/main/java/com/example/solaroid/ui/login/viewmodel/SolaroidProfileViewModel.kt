package com.example.solaroid.ui.login.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.*
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.asDatabaseModel
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.models.room.*
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.repositery.user.UsersRepositery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class SolaroidProfileViewModel(database: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private val dataSource = database

    val profileRepositery =
        ProfileRepostiery(fbAuth, fbDatabase, fbStorage, dataSource, MyProfileDataSource())
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



    private val _firebaseProfile = MutableLiveData<FirebaseProfile>()
    val firebaseProfile: LiveData<FirebaseProfile>
        get() = _firebaseProfile

    val isAlramVisible = Transformations.map(profileType) { type ->
        when (type) {
            ProfileErrorType.ISRIGHT, ProfileErrorType.EMPTY -> false
            else -> true
        }
    }

    val alertMessage = Transformations.map(profileType) { type ->
        "???" + when (type) {
            ProfileErrorType.NICKNAMEERROR -> "????????? ??????????????????"
            ProfileErrorType.IMAGEERROR -> "????????? ????????? ??????????????????"
            else -> ""
        }
    }

    var allUserNum: Long = 0L


    private val _naviToMain = MutableLiveData<Event<Any?>>()
    val naviToMain: LiveData<Event<Any?>>
        get() = _naviToMain


    private val _naviToLogin = MutableLiveData<Event<Any?>>()
    val naviToLogin: LiveData<Event<Any?>>
        get() = _naviToLogin




    private val _addImage = MutableLiveData<Event<Any?>>()
    val addImage: LiveData<Event<Any?>>
        get() = _addImage


    fun onAddBtn() {
        _addImage.value = Event(Unit)
    }

    private val _setProfile = MutableLiveData<Event<Any?>>()
    val setProfile: LiveData<Event<Any?>>
        get() = _setProfile


    private val _participants = MutableLiveData<Int>(0)
    val participants: LiveData<Int>
        get() = _participants


    private val _thumbnail = MutableLiveData<String>("")
    val thumbnail: LiveData<String>
        get() = _thumbnail


    init {
        getFriendCode()
    }


    ///////////////
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

    /**
     * 1.viewModel??? profileType??? setUp??????
     * ?????? ????????? ??????.
     * - ?????? album??? ????????? ???????????????, (????????? ????????? ?????????)
     * - profile??? Firebase??? insert
     * - firebase??? allUserNum ???????????? ???????????????
     * - profile??? ????????? ??????????????? refresh
     * */
    fun insertAndUpdateProfile() {
        viewModelScope.launch {
            insertProfileFirebase()
            updateAllUsersNum()
            refreshProfile()
        }
    }


    /**
     * 2.refreshProfile ???????????? ????????????
     * firebaseProfile??????????????? ?????? setUp?????? ????????? ??????
     *
     * */
    fun insertAndNavigateMain(profile: FirebaseProfile) {
        viewModelScope.launch {
            insertProfile(profile.asDatabaseModel())
            insertUserList(profile)
            navigateToMain()
        }
    }


    /**
     * firebase profile ????????? FirebaseProfile ?????? ??????.
     * */
    suspend fun insertProfileFirebase() {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val profile = FirebaseProfile(
                user.email!!,
                nickname = nickname.value!!,
                profileImg = profileUrl.value!!,
                friendCode = (allUserNum + 1)
            )

            profileRepositery.insertProfileInfo(profile, getApplication())
        }
    }

    /**
     * Download Url??? ?????? firebaseProfile??? ??????.
     * */
    suspend fun refreshProfile() {
        return withContext(Dispatchers.IO) {
            val lambda: (profile: DatabaseProfile) -> Unit = {
                _firebaseProfile.value = it.asFirebaseModel()
            }
            profileRepositery.getProfileInfo(lambda)
        }
    }

    /**
     * allUserNum ??? ?????? 1 ?????? ->?????? ?????? ?????? ????????? Transaction ???????????? ???.
     * */
    suspend fun updateAllUsersNum(): Unit {
        withContext(Dispatchers.IO) {
            usersRepositery.updateAllUserNum(allUserNum + 1L)
        }
    }

    /**
     * Firebase allUsers??? profile ??????.
     * */
    suspend fun insertUserList(profile: FirebaseProfile) {
        withContext(Dispatchers.IO) {
            usersRepositery.insertUsersList(profile)
        }
    }


    companion object {
        const val TAG = "????????? ?????????"
    }


    suspend fun insertProfile(profile: DatabaseProfile) {
        dataSource.insert(profile)
    }

    fun onNicknameEditTextChanged(str: CharSequence) {
        _nickname.value = str.toString()
    }

    fun setProfileUrl(uri: Uri) {
        _profileUrl.value = uri.toString()
    }
    fun onSetProfile() {
        _setProfile.value = Event(Unit)
    }
    fun setProfileType(type: ProfileErrorType) {
        _profileType.value = type
    }
    fun navigateToMain() {
        _naviToMain.value = Event(Unit)
    }

}