package com.ranseo.solaroid.ui.login.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.*
import com.ranseo.solaroid.models.firebase.FirebaseProfile
import com.ranseo.solaroid.models.firebase.asDatabaseModel
import com.ranseo.solaroid.datasource.profile.MyProfileDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.models.room.*
import com.ranseo.solaroid.repositery.profile.ProfileRepostiery
import com.ranseo.solaroid.repositery.user.UsersRepositery
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ranseo.solaroid.utils.FileUtils
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

    var profileImgExif : Int = 0

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
        "※" + when (type) {
            ProfileErrorType.NICKNAMEERROR -> "별명을 입력해주세요"
            ProfileErrorType.IMAGEERROR -> "프로필 사진을 설정해주세요"
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
     * 1.viewModel의 profileType이 setUp되면
     * 해당 메서드 호출.
     * - 현재 album의 개수를 카운트하고, (앨범이 있는지 없는지)
     * - profile을 Firebase에 insert
     * - firebase의 allUserNum 카운트를 증가시키고
     * - profile이 제대로 갱신됐는지 refresh
     * */
    fun insertAndUpdateProfile() {
        viewModelScope.launch {
            insertProfileFirebase()
            updateAllUsersNum()
            refreshProfile()
        }
    }


    /**
     * 2.refreshProfile 메서드가 호출되어
     * firebaseProfile프로퍼티의 값이 setUp되면 메서드 호출
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
     * firebase profile 경로에 FirebaseProfile 객체 삽입.
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

            profileRepositery.insertProfileInfo(profile, getApplication(), profileImgExif)
        }
    }

    /**
     * Download Url을 얻은 firebaseProfile을 얻기.
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
     * allUserNum 의 수를 1 증가 ->동시 접근 하지 않도록 Transaction 추가해야 함.
     * */
    suspend fun updateAllUsersNum(): Unit {
        withContext(Dispatchers.IO) {
            usersRepositery.updateAllUserNum(allUserNum + 1L)
        }
    }

    /**
     * Firebase allUsers에 profile 등록.
     * */
    suspend fun insertUserList(profile: FirebaseProfile) {
        withContext(Dispatchers.IO) {
            usersRepositery.insertUsersList(profile)
        }
    }


    companion object {
        const val TAG = "프로필 뷰모델"
    }


    suspend fun insertProfile(profile: DatabaseProfile) {
        dataSource.insert(profile)
    }

    fun onNicknameEditTextChanged(str: CharSequence) {
        _nickname.value = str.toString()
    }

    fun setProfileUrl(uri: Uri) {
        profileImgExif = FileUtils.getExifAttributeOrientation(uri, getApplication())
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