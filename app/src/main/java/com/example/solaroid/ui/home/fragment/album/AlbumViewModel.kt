package com.example.solaroid.ui.album.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.getAlbumPariticipantsWithFriendCodes
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
import com.example.solaroid.ui.home.fragment.gallery.AIAK
import com.example.solaroid.utils.BitmapUtils
import kotlinx.coroutines.launch

/**
 * albumFragment에는 나의 앨범 목록, 앨범 요청목록 또는 두 가지 모두 보여줘야 하고
 * 이를 RecyclerView의 AlbumListAdapter 내에 submitlist 해야한다.
 * 이를 위해서 다음과 같은 enum class를 만들고, 값을 분류하여 submitList할 수 있도록 설계.
 * */
enum class AlbumType {
    ALL,
    NORMAL,
    REQUEST,
    NONE
}

class AlbumViewModel(dataSource: DatabasePhotoTicketDao) : ViewModel() {
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val roomDB = dataSource

    private val TAG = "AlbumViewModel"

    private val profileRepostiery =
        ProfileRepostiery(fbAuth, fbDatabase, fbStorage, roomDB, MyProfileDataSource())
    private val albumRepostiery = AlbumRepositery(roomDB, fbAuth, fbDatabase, AlbumDataSource())
    private val withAlbumRepositery = WithAlbumRepositery(fbAuth, fbDatabase, WithAlbumDataSource())
    private val albumRequestRepositery =
        AlbumRequestRepositery(fbAuth, fbDatabase, RequestAlbumDataSource())

    private val friendListRepositery =
        FriendListRepositery(fbAuth, fbDatabase, MyFriendListDataSource(), roomDB)


    val myProfile = profileRepostiery.myProfile
    val myFriendList = Transformations.map(friendListRepositery.friendList) {
        convertFriendToDialogFriend(it)
    }

    private fun convertFriendToDialogFriend(list: List<Friend>): List<FriendListDataItem.DialogProfileDataItem> {
        return list.map {
            FriendListDataItem.DialogProfileDataItem(it)
        }
    }

    val albums = albumRepostiery.album


    private val _requestAlbums = MutableLiveData<List<RequestAlbum>>()
    val requestAlbums: LiveData<List<RequestAlbum>>
        get() = _requestAlbums


    private val _album = MutableLiveData<Event<Album>>()
    val album: LiveData<Event<Album>>
        get() = _album

    private val _roomAlbum = MutableLiveData<Event<DatabaseAlbum>>()
    val roomAlbum: LiveData<Event<DatabaseAlbum>>
        get() = _roomAlbum

    private val _requestAlbum = MutableLiveData<Event<RequestAlbum>>()
    val requestAlbum: LiveData<Event<RequestAlbum>>
        get() = _requestAlbum


    private val _albumDataItem = MediatorLiveData<AlbumType>()
    val albumDataItem: LiveData<AlbumType>
        get() = _albumDataItem

    //album을 생서(create) 할 때 사용되는 프로퍼티들

    private val _startCreateAlbum =
        MutableLiveData<Event<List<FriendListDataItem.DialogProfileDataItem>>>()
    val startCreateAlbum: LiveData<Event<List<FriendListDataItem.DialogProfileDataItem>>>
        get() = _startCreateAlbum

    var createId: String = ""
    var createName: String = ""
    var createThumbnail: Bitmap? = null
    var createParticipants: String = ""

    private val _createReady = MutableLiveData<Event<Unit?>>()
    val createReady: LiveData<Event<Unit?>>
        get() = _createReady


    private val _naviToHome = MutableLiveData<Event<Unit>>()
    val naviToHome: LiveData<Event<Unit>>
        get() = _naviToHome

    private val _naviToGallery = MutableLiveData<Event<Album>>()
    val navlToGallery: LiveData<Event<Album>>
        get() = _naviToGallery

    private val _naviToCreate = MutableLiveData<Event<Any?>>()
    val naviToCreate: LiveData<Event<Any?>>
        get() = _naviToCreate


    init {
        with(_albumDataItem) {
            addSource(albums) {
                checkAlbumType(albums, requestAlbums)
            }

            addSource(requestAlbums) {
                checkAlbumType(albums, requestAlbums)
            }
        }
    }


    /**
     * AlbumListAdapter의 onClickListener를
     * 구현할 때 list_item인 Album 객체가 클릭되면
     * 해당 Album 객체를 viewModel 내 album 프로퍼티에 할당
     *  */
    fun setAlbum(album: Album) {
        _album.value = Event(album)
    }

    /**
     * AlbumListAdapter의 onClickListener를
     * 구현할 때 list_item인 RequestAlbum 객체가 클릭되면
     * 해당 RequestAlbum 객체를 viewModel 내 requestAlbum 프로퍼티에 할당
     *  */
    fun setRequestAlbum(album: RequestAlbum) {
        _requestAlbum.value = Event(album)
    }


    /**
     * MediatorLiveData 타입의 albumDataItem의 값을 지정하기 위한 함수로써
     * albums 프로퍼티와 requestAlbum 프로퍼티로의 값을 받아 다음과 같은 조건에 의해
     * 해당 프로퍼티의 값을 지정한다.
     * */
    private fun checkAlbumType(
        normal: LiveData<List<Album>>,
        request: LiveData<List<RequestAlbum>>
    ) {
        _albumDataItem.value =
            if (normal.value.isNullOrEmpty() && request.value.isNullOrEmpty()) AlbumType.NONE
            else if (normal.value.isNullOrEmpty()) AlbumType.REQUEST
            else if (request.value.isNullOrEmpty()) AlbumType.NORMAL
            else AlbumType.ALL

    }

    /**
     * AlbumViewModel이 초기화되고, myProfile 프로퍼티가  초기화되면
     * albumFragment내에서 앨범 목록을 display하기 위한 refresh.
     * albumRepositery와 albumRequestRepositery에 ValueEventListener를 추가하고
     * 각각 alubm과 requestAlbum의 목록을 viewModel 내 프로퍼티 (album 및 requestAlbum) 에
     * 할당할 수 있도록 만드는 함수이다.
     * */
    fun refreshAlubm(myFriendCode: Long) {
        viewModelScope.launch {

            albumRepostiery.addValueEventListener { album ->
                viewModelScope.launch {
                    albumRepostiery.insertRoomAlbum(album)
                }
            }

            albumRequestRepositery.addValueEventListener(myFriendCode) { request ->
                viewModelScope.launch {
                    _requestAlbums.value = request
                }
            }
        }
    }

    /**
     * AlbumFragment UI에서 bottomNavigation의 add 버튼을 누를 때
     * 새로운 앨범을 추가할 수 있는 dialog를 시작하는 함수
     * 나의 친구 목록이 담긴 myFriendList를 _startCreateAlbum의 Event값으로 할당한다.
     * */
    fun onCreateAlbumBtn() {
        viewModelScope.launch {
            val list = myFriendList.value ?: listOf()
            _startCreateAlbum.value = Event(list)
        }
    }

    /**
     * AlbumCreateParticipants 로부터 앨범 참여자들의 list 값를 받아
     * viewModel 내 createParticipants 프로퍼티에 할당한다.
     * 할당할 때 uitls.kt 의 getAlbumParticiapntsWithFriendCodes 함수를 이용하여 해당 list의 friendCode들을 String
     * 타입으로 엮어 반환한 값을 할당한다.
     * */
    fun addParticipants(participants: List<Friend>) {
        createParticipants =
            getAlbumPariticipantsWithFriendCodes(myProfile.value!!.friendCode, participants.map {
                it.friendCode
            })
    }

    /**
     * 앨범을 만들다가 취소할 경우,
     * 관련 create 프로퍼티 값들을 모두 "" 또는 null 로 초기화한다.
     * */
    fun setNullCreateProperty() {
        createParticipants = ""
        createId = ""
        createName = ""
        createThumbnail = null
    }

    /**
     * AlbumCreateDialog에서 앨범 생성을 완료하면
     * 해당 Dialog로 부터 얻은 앨범 값들을 viewModel 내 create 프로퍼티에 할당한다.
     * createId, createName, createThumbnail 등이 있다.
     * */
    fun setCreateProperty(_albumId: String, _albumName: String, _thumbnail: Bitmap) {
        createId = _albumId
        createName = _albumName
        createThumbnail = _thumbnail
    }

    /**
     * AlbumCreateDialog를 완료하고, 모든 create 값을 지정하고 난 뒤,
     * album을 최종적으로 생성하기 전 모든 준비가 끝마쳤음을 알리는 함수
     * */
    fun setCreateReady() {
        _createReady.value = Event(Unit)
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
                participants = createParticipants,
                thumbnail = thumbnail,
                key = ""
            )
            Log.i(TAG,"myProfile.value : ${myProfile.value!!.asFirebaseModel()}, createId : ${createId}")
            withAlbumRepositery.setValue(myProfile.value!!.asFirebaseModel(), createId)

            albumRepostiery.setValue(firebaseAlbum, createId)

            val requestAlbum = FirebaseRequestAlbum(
                id = createId,
                name = createName,
                thumbnail = thumbnail,
                participants = createParticipants,
                key = ""
            )

            albumRequestRepositery.setValueToParticipants(requestAlbum)


        }
    }

    /**
     * RequestAlbum 요청을 수락.
     * 1. 해당 RequestAlbum 객체를 이용하여 withAlbumRepositery에 setValue().
     * firebase - withAlbum - albumId 경로에 내 계정 uid를 write해야
     * album에 접근 및 photoTicket에 접근할 수 있다.
     * */
    fun setValueInWithAlbum(reqAlbum: RequestAlbum) {
        viewModelScope.launch {
            val albumId = reqAlbum.id
            withAlbumRepositery.setValue(myProfile.value!!.asFirebaseModel(), albumId)
        }
    }

    /**
     * RequestAlbum 요청을 수락.
     * 2.albumRepositery에 setValue()
     * firebase - album - uid - albumId 경로에 requestAlbum객체를
     * Album객체로 전환하여 write
     * */
    fun setValueInAlbum(album: Album) {
        viewModelScope.launch {
            val new = FirebaseAlbum(
                album.id,
                album.name,
                BitmapUtils.bitmapToString(album.thumbnail),
                album.participant,
                ""
            )
            albumRepostiery.setValue(new, album.id)
        }
    }

    /**
     * RequestAlbum 요청을 수락 및 거절한 뒤
     * firebase/RequestAlbum/$friendCode 경로에 있는 data 삭제.
     * */
    fun deleteRequestAlbumInFirebase(album: RequestAlbum) {
        viewModelScope.launch {
            albumRequestRepositery.deleteValue(
                convertHexStringToLongFormat(myProfile.value!!.friendCode),
                album.key
            )
        }
    }


    /**
     * List_Item의 Album객체를 클릭 했을 때 해당 Album객체의 key를 이용해
     * Room Database Album 객체를 get()하는 함수
     * */
    fun getRoomDatabaseAlbum(albumId: String) {
        viewModelScope.launch {
            _roomAlbum.value = Event(roomDB.getAlbum(albumId))
        }
    }

    fun removeListener() {
        albumRequestRepositery.removeListener(myProfile.value!!.friendCode.drop(1))
        albumRepostiery.removeListener()
    }

    //navigate
    fun navigateToHome() {
        _naviToHome.value = Event(Unit)
    }

    fun navigateToCreate() {
        _naviToCreate.value = Event(Unit)
    }


}