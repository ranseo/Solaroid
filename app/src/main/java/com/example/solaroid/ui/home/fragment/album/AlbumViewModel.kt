package com.example.solaroid.ui.album.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.datasource.friend.MyFriendListDataSource
import com.example.solaroid.datasource.profile.MyProfileDataSource
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.getAlbumPariticipantsWithFriendCodes
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.AlbumRequestRepositery
import com.example.solaroid.repositery.album.WithAlbumRepositery
import com.example.solaroid.repositery.friend.FriendListRepositery
import com.example.solaroid.repositery.profile.ProfileRepostiery
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.utils.BitmapUtils
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

    private val _requestAlbum = MutableLiveData<List<RequestAlbum>>()
    val requestAlbum: LiveData<List<RequestAlbum>>
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
    val createReady : LiveData<Event<Unit?>>
        get() = _createReady


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

    init {
        with(_albumDataItem) {
            addSource(albums) {
                checkAlbumType(albums, requestAlbum)
            }

            addSource(requestAlbum) {
                checkAlbumType(albums, requestAlbum)
            }
        }
    }


    fun refreshAlubm(myFriendCode: Long) {
        viewModelScope.launch {

            albumRepostiery.addValueEventListener { album ->
                viewModelScope.launch {
                    albumRepostiery.insertRoomAlbum(album)
                }
            }

            albumRequestRepositery.addValueEventListener(myFriendCode) { request ->
                viewModelScope.launch {
                    _requestAlbum.value = request
                }
            }
        }
    }

    fun onCreateAlbumBtn() {
        viewModelScope.launch {
            val list = myFriendList.value ?: listOf()
            _startCreateAlbum.value = Event(list)
        }
    }

    fun addParticipants(participants: List<Friend>) {

        createParticipants = getAlbumPariticipantsWithFriendCodes(participants.map {
            it.friendCode
        }+ myProfile.value!!.friendCode)
    }

    fun setNullCreateProperty() {
        createParticipants = ""
        createId = ""
        createName = ""
        createThumbnail = null
    }

    fun setCreateProperty(_albumId: String, _albumName:String, _thumbnail:Bitmap) {
        createId = _albumId
        createName = _albumName
        createThumbnail = _thumbnail
    }

    fun setCreateReady() {
        _createReady.value = Event(Unit)
    }


    fun createAlbum() {
        viewModelScope.launch {
            val firebaseAlbum = FirebaseAlbum(
                id = createId,
                name = createName,
                participants = createParticipants,
                thumbnail = BitmapUtils.bitmapToString(createThumbnail!!),
                key = ""
            )


            albumRepostiery.setValue(firebaseAlbum, createId)

            val requestAlbum = RequestAlbum(
                id = createId,
                name = createName,
                participant = createParticipants
            )

            albumRequestRepositery.setValueToParticipants(requestAlbum)
        }
    }

    fun removeListener() {
        albumRequestRepositery.removeListener(myProfile.value!!.friendCode.drop(1))
        albumRepostiery.removeListener()
    }


}