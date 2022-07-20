package com.example.solaroid.ui.home.fragment.album.create

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.*
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.models.domain.*
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.models.firebase.FirebaseRequestAlbum
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.AlbumRequestRepositery
import com.example.solaroid.repositery.album.WithAlbumRepositery
import com.example.solaroid.repositery.friend.FriendListRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.NullPointerException

class AlbumCreateViewModel(dataSource: DatabasePhotoTicketDao) : ViewModel() {
    private val TAG = "AlbumCreateViewModel"

    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val roomDB = dataSource


    private val profileRepostiery =
        ProfileRepostiery(fbAuth, fbDatabase, fbStorage, roomDB, MyProfileDataSource())
    private val albumRepostiery = AlbumRepositery(roomDB, fbAuth, fbDatabase, AlbumDataSource())
    private val withAlbumRepositery = WithAlbumRepositery(fbAuth, fbDatabase, WithAlbumDataSource())
    private val albumRequestRepositery =
        AlbumRequestRepositery(fbAuth, fbDatabase, RequestAlbumDataSource())

    private val friendListRepositery =
        FriendListRepositery(fbAuth, fbDatabase, MyFriendListDataSource(), roomDB)


    lateinit var myProfile : LiveData<Profile>
    lateinit var myFriendList : LiveData<List<FriendListDataItem.DialogProfileDataItem>>
    private fun convertFriendToDialogFriend(list: List<Friend>): List<FriendListDataItem.DialogProfileDataItem> {
        return list.map {
            FriendListDataItem.DialogProfileDataItem(it)
        }
    }


    private val _album = MutableLiveData<Event<Album>>()
    val album: LiveData<Event<Album>>
        get() = _album

    private val _roomAlbum = MutableLiveData<Event<DatabaseAlbum>>()
    val roomAlbum: LiveData<Event<DatabaseAlbum>>
        get() = _roomAlbum

    private val _requestAlbum = MutableLiveData<Event<RequestAlbum>>()
    val requestAlbum: LiveData<Event<RequestAlbum>>
        get() = _requestAlbum

    //album을 생성(create) 할 때 사용되는 프로퍼티들
    var createThumbnail: Bitmap? = null

    private val _participants = MutableLiveData<List<Friend>>()
    val participants: LiveData<List<Friend>>
        get() = _participants



    //Final에서 쓰일 프로퍼티
    val createId = Transformations.map(participants) {
        if (!it.isNullOrEmpty()) {
            getAlbumIdWithFriendCodes(it.map { v -> v.friendCode })
        } else ""
    }

    val createName = Transformations.map(participants) {
        if (!it.isNullOrEmpty()) {
            getAlbumNameWithFriendsNickname(it.map { v -> v.nickname }, myProfile!!.value!!.nickname)
        } else ""
    }

    val createBitmap = Transformations.map(participants) {
        if (!it.isNullOrEmpty()) {
            joinProfileImgListToString(listOf(myProfile!!.value!!.profileImg) + it.map { v -> v.profileImg })
        } else "acac"
    }
    ///


    val participantsListString = Transformations.map(participants) {
        "참여자 : " + it.fold("${myProfile!! .value!!.nickname}, ") { acc, v ->
            acc + v.nickname + ", "
        }.dropLast(2)
    }

    private val _naviToAlbum = MutableLiveData<Event<Any?>>()
    val naviToAlbum: LiveData<Event<Any?>>
        get() = _naviToAlbum


    init {
        viewModelScope.launch {
            myProfile = profileRepostiery.myProfile

            Log.i(TAG, "init() myProfile : ${myProfile?.value?.nickname}}")

            myFriendList = Transformations.map(friendListRepositery.friendList) {
                convertFriendToDialogFriend(it)
            }

            Log.i(TAG, "init() myFriendList : ${myFriendList?.value}")
        }
    }

    /**
     * createAlbum()을 호출하고 완료한 뒤에
     * navigateToAlbum()호출
     * */
    fun createAndNavigate() {
        viewModelScope.launch {
            createAlbum()
            withContext(Dispatchers.Main) {
                navigateToAlbum()
            }
        }
    }

    /**
     * Album 및 FirebaseAlbum 객체를 만들고 해당 객체를
     * Album과 관련된 Repositery의 setValue() 메서드에 전달하여
     * firebase 경로와 Room Database에 앨범을 생성하는 함수.
     * 앨범의 참여자들에게 RequsetAlbum 객체를 전달할 수 있도록 만든다.
     * */
    suspend private fun createAlbum() {
        withContext(Dispatchers.IO){
            try {
                val thumbnail = BitmapUtils.bitmapToString(createThumbnail!!)
                val firebaseAlbum = FirebaseAlbum(
                    id = createId.value!!,
                    name = createName.value!!,
                    participants = convertFriendListToString(),
                    thumbnail = thumbnail,
                    key = ""
                )
                Log.i(
                    TAG,
                    "myProfile.value : ${myProfile!!.value!!.asFirebaseModel()}, createId : ${createId}"
                )
                withAlbumRepositery.setValue(myProfile!!.value!!.asFirebaseModel(), createId.value!!)

                albumRepostiery.setValue(firebaseAlbum, createId.value!!)

                val requestAlbum = FirebaseRequestAlbum(
                    id = createId.value!!,
                    name = createName.value!!,
                    thumbnail = thumbnail,
                    participants = convertFriendListToString(),
                    key = ""
                )

                albumRequestRepositery.setValueToParticipants(requestAlbum)


            } catch (error: IOException) {
                error.printStackTrace()
            } catch (error: NullPointerException) {
                error.printStackTrace()
            }

        }
    }

    /**
     * viewModel - participants 프로퍼티의 값을 할당하는 메서드
     * */
    fun setParticipants(list: List<Friend>) {
        _participants.value = list

    }

    /**
     * viewModel - createThumbnail 프로퍼티의 값을 설정하는 메서드
     * */
    fun setThumbnail(bitmap: Bitmap) {
        createThumbnail = bitmap
    }

    /**
     * AlbumCreateStart 로부터 앨범 참여자들의 list 값를 받아
     * viewModel 내 createParticipants 프로퍼티에 할당한다.
     * 할당할 때 uitls.kt 의 getAlbumParticiapntsWithFriendCodes 함수를 이용하여 해당 list의 friendCode들을 String
     * 타입으로 엮어 반환한 값을 할당한다.
     * */
    private fun convertFriendListToString(): String {
        val list = participants.value ?: listOf()
        return getAlbumPariticipantsWithFriendCodes(myProfile!!.value!!.friendCode, list.map {
            it.friendCode
        })
    }

    /**
     * 앨범을 만들다가 취소할 경우,
     * 관련 create 프로퍼티 값들을 모두 "" 또는 null 로 초기화한다.
     * */
    fun setNullCreateProperty() {
        _participants.value = listOf()
        createThumbnail = null
    }

    /**
     * AlbumCreateart 프래그먼트에서 "다음" 버튼을 누르면
     * ViewModel - participants 프로퍼티의 값이 null 이라면 (아무 친구도 선택하지 않아서)
     * listOf() 를 할당. 그렇지 않다면 그대로
     * */
    fun checkParticipants() {
        if(_participants.value==null) {
            _participants.value= listOf()
        }
    }


    fun removeListener() {
        albumRequestRepositery.removeListener(myProfile!!.value!!.friendCode.drop(1))
        albumRepostiery.removeListener()
    }

    //navigate
    fun navigateToAlbum() {
        _naviToAlbum.value = Event(Unit)
    }

}