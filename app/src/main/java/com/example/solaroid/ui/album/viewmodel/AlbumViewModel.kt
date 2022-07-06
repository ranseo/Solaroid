package com.example.solaroid.ui.album.viewmodel

import androidx.lifecycle.*
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.AlbumRequestRepositery
import com.example.solaroid.repositery.album.WithAlbumRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import kotlinx.coroutines.launch

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

    private val profileRepostiery =
        ProfileRepostiery(fbAuth, fbDatabase, fbStorage, roomDB, MyProfileDataSource())
    private val albumRepostiery = AlbumRepositery(roomDB, fbAuth, fbDatabase, AlbumDataSource())
    private val wtihAlbumRepositery = WithAlbumRepositery(fbAuth, fbDatabase, WithAlbumDataSource())
    private val albumRequestRepositery =
        AlbumRequestRepositery(fbAuth, fbDatabase, RequestAlbumDataSource())

    val myProfile = profileRepostiery.myProfile
    val albums = albumRepostiery.album

    private val _requestAlbum = MutableLiveData<List<RequestAlbum>>()
    val requestAlbum: LiveData<List<RequestAlbum>>
        get() = _requestAlbum

    private val _albumDataItem = MediatorLiveData<AlbumType>()
    val albumDataItem : LiveData<AlbumType>
        get() = _albumDataItem


    private fun checkAlbumType(normal:LiveData<List<Album>>, request:LiveData<List<RequestAlbum>>) {
        _albumDataItem.value =
            if(normal.value.isNullOrEmpty() && request.value.isNullOrEmpty()) AlbumType.NONE
            else if(normal.value.isNullOrEmpty()) AlbumType.REQUEST
            else if(request.value.isNullOrEmpty()) AlbumType.NORMAL
            else AlbumType.ALL

    }
    init {
        with(_albumDataItem) {
            addSource(albums) {
                checkAlbumType(albums,requestAlbum)
            }

            addSource(requestAlbum) {
                checkAlbumType(albums,requestAlbum)
            }
        }
    }


    fun refreshAlubm(myFriendCode:Long) {
        viewModelScope.launch {

            albumRepostiery.addValueEventListener { album ->
                viewModelScope.launch {
                    albumRepostiery.insertRoomAlbum(album)
                }
            }

            albumRequestRepositery.addValueEventListener(myFriendCode){ request ->
                viewModelScope.launch {
                    _requestAlbum.value = request
                }
            }
        }
    }

    fun onCreateAlbum() {

    }




}