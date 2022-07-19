package com.example.solaroid.ui.home.fragment.album.create

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.getAlbumPariticipantsWithFriendCodes
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.domain.asFirebaseModel
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.models.firebase.FirebaseRequestAlbum
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.AlbumRequestRepositery
import com.example.solaroid.repositery.album.WithAlbumRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.utils.BitmapUtils
import kotlinx.coroutines.launch

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

    val myProfile = profileRepostiery.myProfile


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

    var createId: String = ""
    var createName: String = ""
    var createThumbnail: Bitmap? = null

    private val _participants = MutableLiveData<List<Friend>>()
    val participants: LiveData<List<Friend>>
        get() = _participants


    val participantsListString = Transformations.map(participants) {
        "참여자 : " + it.fold("${myProfile.value!!.nickname}, ") { acc, v ->
            acc + v.nickname + ", "
        }.dropLast(2)
    }


    private val _createReady = MutableLiveData<Event<Unit?>>()
    val createReady: LiveData<Event<Unit?>>
        get() = _createReady


    private val _naviToAlbum = MutableLiveData<Event<Any?>>()
    val naviToCreate: LiveData<Event<Any?>>
        get() = _naviToAlbum


    init {

    }

    /**
     * Album 및 FirebaseAlbum 객체를 만들고 해당 객체를
     * Album과 관련된 Repositery의 setValue() 메서드에 전달하여
     * firebase 경로와 Room Database에 앨범을 생성하는 함수.
     * 앨범의 참여자들에게 RequsetAlbum 객체를 전달할 수 있도록 만든다.
     * */
    fun createAlbum() {
        viewModelScope.launch {
            val thumbnail = BitmapUtils.bitmapToString(createThumbnail!!)
            val firebaseAlbum = FirebaseAlbum(
                id = createId,
                name = createName,
                participants = convertFriendListToString(),
                thumbnail = thumbnail,
                key = ""
            )
            Log.i(
                TAG,
                "myProfile.value : ${myProfile.value!!.asFirebaseModel()}, createId : ${createId}"
            )
            withAlbumRepositery.setValue(myProfile.value!!.asFirebaseModel(), createId)

            albumRepostiery.setValue(firebaseAlbum, createId)

            val requestAlbum = FirebaseRequestAlbum(
                id = createId,
                name = createName,
                thumbnail = thumbnail,
                participants = convertFriendListToString(),
                key = ""
            )

            albumRequestRepositery.setValueToParticipants(requestAlbum)


        }
    }

    /**
     * viewModel - participants 프로퍼티의 값을 할당하는 메서드
     * */
    fun setParticipants(list: List<Friend>) {
        _participants.value = list

    }

    /**
     * AlbumCreateStart 로부터 앨범 참여자들의 list 값를 받아
     * viewModel 내 createParticipants 프로퍼티에 할당한다.
     * 할당할 때 uitls.kt 의 getAlbumParticiapntsWithFriendCodes 함수를 이용하여 해당 list의 friendCode들을 String
     * 타입으로 엮어 반환한 값을 할당한다.
     * */
    private fun convertFriendListToString(): String {
        val list = participants.value ?: listOf()
        return getAlbumPariticipantsWithFriendCodes(myProfile.value!!.friendCode, list.map {
            it.friendCode
        })
    }

    /**
     * AlbumCreateFinal 에서 앨범 생성을 클릭하면
     * 해당 Fragment로 부터 얻은 앨범 값들을 viewModel 내 create 프로퍼티에 할당한다.
     * createId, createName, createThumbnail 등이 있다.
     * */
    fun setCreateProperty(_albumId: String, _albumName: String, _thumbnail: Bitmap) {
        createId = _albumId
        createName = _albumName
        createThumbnail = _thumbnail
    }

    /**
     * 앨범을 만들다가 취소할 경우,
     * 관련 create 프로퍼티 값들을 모두 "" 또는 null 로 초기화한다.
     * */
    fun setNullCreateProperty() {
        _participants.value = listOf()
        createId = ""
        createName = ""
        createThumbnail = null
    }


    /**
     * AlbumCreateFinal에서 생성버튼을 누르고, viewModel.setCreateProperty()가 호출되어 모든 create 값을 지정하고 나면
     * viewModel.createAlbum()를 호출하기 위해 _createReady 프로퍼티 값을 할당하는 메서드
     * */
    fun setCreateReady() {
        _createReady.value = Event(Unit)
    }


    fun removeListener() {
        albumRequestRepositery.removeListener(myProfile.value!!.friendCode.drop(1))
        albumRepostiery.removeListener()
    }

}