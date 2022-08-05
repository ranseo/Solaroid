package com.ranseo.solaroid.ui.album.viewmodel

import androidx.lifecycle.*
import com.ranseo.solaroid.Event
import com.ranseo.solaroid.datasource.album.AlbumDataSource
import com.ranseo.solaroid.datasource.album.WithAlbumDataSource
import com.ranseo.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.ranseo.solaroid.datasource.profile.MyProfileDataSource
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.models.domain.Album
import com.ranseo.solaroid.models.room.DatabaseAlbum
import com.ranseo.solaroid.models.room.asFirebaseModel
import com.ranseo.solaroid.parseAlbumParticipantsAndGetParticpantsNum
import com.ranseo.solaroid.repositery.album.AlbumRepositery
import com.ranseo.solaroid.repositery.album.WithAlbumRepositery
import com.ranseo.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.ranseo.solaroid.repositery.profile.ProfileRepostiery
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import kotlinx.coroutines.launch

//DatabaseAlbum과 Tag를 Pair로 짝지어, list_item album 객체를 click 또는 long click 했을 때
//Tag에 따라 구분할 수 있게 만들기.
typealias AlbumTag = Pair<Album, ClickTag>
typealias DAlbumTag = Pair<DatabaseAlbum, ClickTag>

enum class ClickTag {
    CLICK,
    LONG,
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
    private val photoTicketRepositery = PhotoTicketRepositery(
        roomDB,
        fbAuth,
        fbDatabase,
        fbStorage,
        PhotoTicketListenerDataSource()
    )

    val myProfile = profileRepostiery.myProfile

    val albums = albumRepostiery.album



    private val _album = MutableLiveData<Event<AlbumTag?>>()
    val album: LiveData<Event<AlbumTag?>>
        get() = _album

    private val _roomAlbum = MutableLiveData<Event<DAlbumTag?>>()
    val roomAlbum: LiveData<Event<DAlbumTag?>>
        get() = _roomAlbum


    private val _naviToHome = MutableLiveData<Event<Unit>>()
    val naviToHome: LiveData<Event<Unit>>
        get() = _naviToHome

    private val _naviToPhotoCreate = MutableLiveData<Event<Unit>>()
    val naviToPhotoCreate: LiveData<Event<Unit>>
        get() = _naviToPhotoCreate

    private val _naviToCreate = MutableLiveData<Event<Any?>>()
    val naviToCreate: LiveData<Event<Any?>>
        get() = _naviToCreate

    private val _naviToRequest = MutableLiveData<Event<Any?>>()
    val naviToRequest: LiveData<Event<Any?>>
        get() = _naviToRequest

    private val _naviToGallery = MutableLiveData<Event<Album>>()
    val navlToGallery: LiveData<Event<Album>>
        get() = _naviToGallery


    init {
    }


    /**
     * AlbumListAdapter의 onClickListener를
     * 구현할 때 list_item인 Album 객체가 클릭되면
     * 해당 Album 객체를 viewModel 내 album 프로퍼티에 할당
     *  */
    fun setAlbum(albumTag: AlbumTag) {
        _album.value = Event(albumTag)
    }


    /**
     * AlbumViewModel이 초기화되고, myProfile 프로퍼티가  초기화되면
     * albumFragment내에서 앨범 목록을 display하기 위한 refresh.
     * albumRepositery와 albumRequestRepositery에 ValueEventListener를 추가하고
     * 각각 alubm과 requestAlbum의 목록을 viewModel 내 프로퍼티 (album 및 requestAlbum) 에
     * 할당할 수 있도록 만드는 함수이다.
     * */
    fun refreshAlbum() {
        viewModelScope.launch {
            albumRepostiery.addValueEventListener { albums ->
                viewModelScope.launch {
                    albumRepostiery.insertRoomAlbums(albums)
                }
            }

        }
    }


    /**
     * List_Item의 Album객체를 클릭 했을 때 해당 Album객체의 key를 이용해
     * Room Database Album 객체를 get()하는 함수
     * */
    fun getRoomDatabaseAlbum(albumId: String, tag: ClickTag) {
        viewModelScope.launch {
            _roomAlbum.value = Event(DAlbumTag(roomDB.getAlbum(albumId), tag))
        }
    }


    /**
     * Album을 Long Click -> Dialog 나타남 -> 삭제를 선택했을 때
     * deleteAlbum
     * */
    fun deleteCurrAlbum(album: DatabaseAlbum) {
        viewModelScope.launch {
            albumRepostiery.deleteAlbumInFirebase(album.asFirebaseModel())
            albumRepostiery.deleteAlbumInRoomDB(album)

            if (parseAlbumParticipantsAndGetParticpantsNum(album.participants) == 1) {
                photoTicketRepositery.deletePhotoTickets(album.asFirebaseModel())
            }
            photoTicketRepositery.deletePhotoTicketsInRoom(album.id)
            withAlbumRepositery.removeWithAlbumValue(album.asFirebaseModel())

        }
    }


    /**
     * 해당 사진첩의 이름을 변경하는 앨범 편집 메서드로써
     * firebase 내 앨범이름을 변경한다.
     * */
    fun editAlbum(name: String) {
        viewModelScope.launch {
            try {
                val new = roomAlbum.value!!.peekContent()!!.first.asFirebaseModel(name)
                albumRepostiery.editAlbum(new)
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }
    }


    fun removeListener() {
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

    fun navigateToRequest() {
        _naviToRequest.value = Event(Unit)
    }


}