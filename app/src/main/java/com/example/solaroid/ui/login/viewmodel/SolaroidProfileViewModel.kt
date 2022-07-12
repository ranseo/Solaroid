package com.example.solaroid.ui.login.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.example.solaroid.*
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.asDatabaseModel
import com.example.solaroid.models.room.DatabaseProfile
import com.example.solaroid.models.room.asFirebaseModel
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.repositery.user.UsersRepositery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.utils.BitmapUtils
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
    val albumRepostiery = AlbumRepositery(dataSource, fbAuth, fbDatabase, AlbumDataSource())

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

    private val _firebaseProfile = MutableLiveData<FirebaseProfile>()
    val firebaseProfile: LiveData<FirebaseProfile>
        get() = _firebaseProfile

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

    private val _isAlbum = MutableLiveData<Boolean>()
    val isAlbum: LiveData<Boolean>
        get() = _isAlbum


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

    init {
//        isProfileAlready()
        getFriendCode()
    }


    ///////////////
    private fun isProfileAlready() {
        viewModelScope.launch {
            profileRepositery.isProfile()
                ?.addOnSuccessListener {
                    val data = it.value
                    if (data != null) _naviToMain.value = Event(Unit)
                }
        }
    }


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

    fun insertAndUpdateProfile() {
        viewModelScope.launch {
            getAlbumCount()
            insertProfileFirebase()
            updateAllUsersNum()
            refreshProfile()
        }
    }

    fun insertAndNavigateMain(profile: FirebaseProfile) {
        viewModelScope.launch {
            setValueFirstAlbum(profile)
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

            profileRepositery.insertProfileInfo(profile, getApplication())
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

    /**
     * isAlbum 초기화
     * */
    suspend fun getAlbumCount() {
        albumRepostiery.addGetAlbumCountSingleValueEventListener { cnt ->
            _isAlbum.value = when (cnt) {
                0 -> false
                else -> true
            }
        }
    }

    /**
     * 만약 isAlbum의 값이 false일 경우, 아직 아무런 album이 생성되어있지 않다는 뜻이므로
     * 새로운 앨범을 만들고 해당 앨범을 홈 앨범으로 만든다.
     * */
    suspend fun setValueFirstAlbum(profile: FirebaseProfile) {
        val friendCode = convertLongToHexStringFormat(profile.friendCode)
        val albumId = getAlbumIdWithFriendCodes(listOf(friendCode), 0)
        val albumName = getAlbumNameWithFriendsNickname(listOf(profile.nickname))
        val participants = getAlbumPariticipantsWithFriendCodes(listOf(friendCode))
        val byteArray = BitmapUtils.convertUriToByteArray(profile.profileImg.toUri(),getApplication())
        val firebaseAlbum = FirebaseAlbum(
            albumId,
            albumName,
            byteArray!!,
            participants,
            ""
        )

        albumRepostiery.setValue(firebaseAlbum, albumId)
    }

    companion object {
        const val TAG = "프로필 뷰모델"
    }


    suspend fun insertProfile(profile: DatabaseProfile) {
        dataSource.insert(profile)
    }


}