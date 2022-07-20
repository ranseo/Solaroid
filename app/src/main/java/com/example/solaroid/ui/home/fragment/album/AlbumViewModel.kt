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

    val myProfile = profileRepostiery.myProfile

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


    private val _naviToHome = MutableLiveData<Event<Unit>>()
    val naviToHome: LiveData<Event<Unit>>
        get() = _naviToHome

    private val _naviToPhotoCreate = MutableLiveData<Event<Unit>>()
    val naviToPhotoCreate : LiveData<Event<Unit>>
        get() = _naviToPhotoCreate

    private val _naviToCreate = MutableLiveData<Event<Any?>>()
    val naviToCreate: LiveData<Event<Any?>>
        get() = _naviToCreate

    private val _naviToGallery = MutableLiveData<Event<Album>>()
    val navlToGallery: LiveData<Event<Album>>
        get() = _naviToGallery


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

    fun navigateToPhotoCreate() {
        _naviToPhotoCreate.value = Event(Unit)
    }






}