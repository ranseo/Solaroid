package com.example.solaroid.ui.album.viewmodel

import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.domain.asFirebaseModel
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.AlbumRequestRepositery
import com.example.solaroid.repositery.album.WithAlbumRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.ui.album.adapter.AlbumListDataItem
import com.example.solaroid.ui.album.adapter.AlbumListItemCallback
import com.example.solaroid.utils.BitmapUtils
import kotlinx.coroutines.launch


class AlbumViewModel(dataSource: DatabasePhotoTicketDao) : ViewModel() {
    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val roomDB = dataSource

    private val TAG = "AlbumViewModel"

    private val profileRepostiery =
        ProfileRepostiery(fbAuth, fbDatabase, fbStorage, roomDB, MyProfileDataSource())
    private val albumRepostiery = AlbumRepositery(roomDB, fbAuth, fbDatabase, AlbumDataSource())

    val myProfile = profileRepostiery.myProfile

    val albums = Transformations.map(albumRepostiery.album) {
        it?.let {
            it.map { v -> AlbumListDataItem.NormalAlbumDataItem(v) }
        }
    }


    private val _album = MutableLiveData<Event<Album?>>()
    val album: LiveData<Event<Album?>>
        get() = _album

    private val _roomAlbum = MutableLiveData<Event<DatabaseAlbum>>()
    val roomAlbum: LiveData<Event<DatabaseAlbum>>
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
    val naviToRequest : LiveData<Event<Any?>>
        get() = _naviToRequest

    private val _naviToGallery = MutableLiveData<Event<Album>>()
    val navlToGallery: LiveData<Event<Album>>
        get() = _naviToGallery


    init {
    }


    /**
     * AlbumListAdapter??? onClickListener???
     * ????????? ??? list_item??? Album ????????? ????????????
     * ?????? Album ????????? viewModel ??? album ??????????????? ??????
     *  */
    fun setAlbum(album: Album) {
        _album.value = Event(album)
    }


    /**
     * AlbumViewModel??? ???????????????, myProfile ???????????????  ???????????????
     * albumFragment????????? ?????? ????????? display?????? ?????? refresh.
     * albumRepositery??? albumRequestRepositery??? ValueEventListener??? ????????????
     * ?????? alubm??? requestAlbum??? ????????? viewModel ??? ???????????? (album ??? requestAlbum) ???
     * ????????? ??? ????????? ????????? ????????????.
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
     * List_Item??? Album????????? ?????? ?????? ??? ?????? Album????????? key??? ?????????
     * Room Database Album ????????? get()?????? ??????
     * */
    fun getRoomDatabaseAlbum(albumId: String) {
        viewModelScope.launch {
            _roomAlbum.value = Event(roomDB.getAlbum(albumId))
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