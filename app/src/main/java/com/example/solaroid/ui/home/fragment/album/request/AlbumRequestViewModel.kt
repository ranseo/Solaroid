package com.example.solaroid.ui.home.fragment.album.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.AlbumRequestRepositery
import com.example.solaroid.repositery.album.WithAlbumRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.ui.album.adapter.AlbumListDataItem
import com.example.solaroid.utils.BitmapUtils
import kotlinx.coroutines.launch

class AlbumRequestViewModel(dataSource: DatabasePhotoTicketDao) : ViewModel() {
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

    private val _requestAlbums = MutableLiveData<List<AlbumListDataItem.RequestAlbumDataItem>>()
    val requestAlbums: LiveData<List<AlbumListDataItem.RequestAlbumDataItem>>
        get() = _requestAlbums

    private val _requestAlbum = MutableLiveData<Event<RequestAlbum?>>()
    val requestAlbum : LiveData<Event<RequestAlbum?>>
        get() = _requestAlbum

    init {

    }


    fun refreshRequestAlbums(myFriendCode: String) {
        viewModelScope.launch {
            albumRequestRepositery.addValueEventListener(myFriendCode) { request ->
                viewModelScope.launch {
                    _requestAlbums.value = request.map { v -> AlbumListDataItem.RequestAlbumDataItem(v) }
                }
            }
        }
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
     * RequestAlbum 요청을 수락.
     * 1. 해당 RequestAlbum 객체를 이용하여 withAlbumRepositery에 setValue().
     * firebase - withAlbum - albumId 경로에 내 계정 uid를 write해야
     * album에 접근 및 photoTicket에 접근할 수 있다.
     * */
    fun setValueInWithAlbum(album: RequestAlbum) {
        viewModelScope.launch {
            withAlbumRepositery.setValue(myProfile.value!!.asFirebaseModel(), album.id, album.key)
        }
    }

    /**
     * RequestAlbum 요청을 수락.
     * 2.albumRepositery에 setValue()
     * firebase - album - uid - albumId 경로에 requestAlbum객체를
     * Album객체로 전환하여 write
     * */
    fun setValueInAlbum(album: FirebaseAlbum) {
        viewModelScope.launch {
            albumRepostiery.setValueInRequestAlbum(album, album.id)
        }
    }


    /**
     * RequestAlbum 요청을 수락 및 거절한 뒤
     * firebase/RequestAlbum/$friendCode 경로에 있는 data 삭제.
     * */
    fun deleteRequestAlbumInFirebase(album: RequestAlbum) {
        viewModelScope.launch {
            albumRequestRepositery.deleteValue(
                myProfile.value!!.friendCode.drop(1),
                album.key
            )
        }
    }


    fun removeListener() {
        albumRequestRepositery.removeListener(myProfile.value!!.friendCode.drop(1))
    }




}