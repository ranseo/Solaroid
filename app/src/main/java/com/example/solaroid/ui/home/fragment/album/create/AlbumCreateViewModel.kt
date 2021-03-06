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


    val myProfile: LiveData<Profile> = profileRepostiery.myProfile
    val myFriendList: LiveData<List<FriendListDataItem.DialogProfileDataItem>> =
        Transformations.map(friendListRepositery.friendList) {
            it?.let { list ->
                convertFriendToDialogFriend(list)
            }
        }

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

    //album??? ??????(create) ??? ??? ???????????? ???????????????
    var createThumbnail: Bitmap? = null

    private val _participants = MutableLiveData<List<Friend>>()
    val participants: LiveData<List<Friend>>
        get() = _participants


    //new Album??? ?????? ???, ?????? Album??? key??? requestAlbum?????? ????????? ????????????.
    //????????? albumKey ??????????????? AlbumRepositery.setValue() ??? ????????????(key:String)->Unit??? ????????????
    //albumKey??? ?????? ????????????, observe ?????? ????????? requestAlbum??? ????????????.
    private val _albumKey = MutableLiveData<String>()
    val albumKey: LiveData<String>
        get() = _albumKey


    private val _profileAndParticipants = MediatorLiveData<List<Friend>>()
    private val profileAndParticipants: LiveData<List<Friend>>
        get() = _profileAndParticipants

    fun setProfileAndParticipants(
        myProfile: LiveData<Profile>,
        participants: LiveData<List<Friend>>
    ) {
        if (myProfile.value != null && participants.value != null) {
            _profileAndParticipants.value =
                listOf(myProfile.value!!.asFriend("")) + participants.value!!
        }
    }


    //Final?????? ?????? ????????????
    val createParticipants = Transformations.map(profileAndParticipants) {
        if (!it.isNullOrEmpty()) {
            getAlbumParticipantsWithFriendCodes(it.map { v -> v.friendCode })
        } else ""
    }

    val createId = Transformations.map(profileAndParticipants) {
        if (!it.isNullOrEmpty()) {
            getAlbumIdWithFriendCodes(it.map { v -> v.friendCode })
        } else ""
    }

    val createName = Transformations.map(profileAndParticipants) {
        if (!it.isNullOrEmpty()) {
            getAlbumNameWithFriendsNickname(it.map { v -> v.nickname })
        } else ""
    }

    val createBitmap = Transformations.map(profileAndParticipants) {
        if (!it.isNullOrEmpty()) {
            joinProfileImgListToString(it.map { v -> v.profileImg })
        } else ""
    }


    ///


    val participantsListString = Transformations.map(participants) {
        "????????? : " + it.fold("${myProfile.value!!.nickname}, ") { acc, v ->
            acc + v.nickname + ", "
        }.dropLast(2)
    }

    private val _naviToAlbum = MutableLiveData<Event<Any?>>()
    val naviToAlbum: LiveData<Event<Any?>>
        get() = _naviToAlbum


    init {
        with(_profileAndParticipants) {
            addSource(myProfile) {
                setProfileAndParticipants(myProfile, participants)
            }
            addSource(participants) {
                setProfileAndParticipants(myProfile, participants)
            }
        }
    }

    /**
     * createAlbum()??? ???????????? ????????? ??????
     * navigateToAlbum()??????
     * */
    fun createAndNavigate() {
        viewModelScope.launch {
            createAlbum()

        }
    }

    /**
     * Album ??? FirebaseAlbum ????????? ????????? ?????? ?????????
     * Album??? ????????? Repositery??? setValue() ???????????? ????????????
     * firebase ????????? Room Database??? ????????? ???????????? ??????.
     * ????????? ?????????????????? RequsetAlbum ????????? ????????? ??? ????????? ?????????.
     * */
    private suspend fun createAlbum() {
        withContext(Dispatchers.IO) {
            try {
                val thumbnail = BitmapUtils.bitmapToString(createThumbnail!!)

                Log.i(
                    TAG,
                    "createId : ${createId.value}, createName : ${createName.value}, createParticipants : ${createParticipants.value} "
                )

                val firebaseAlbum = FirebaseAlbum(
                    id = createId.value!!,
                    name = createName.value!!,
                    participants = createParticipants.value!!,
                    thumbnail = thumbnail,
                    key = ""
                )

                withAlbumRepositery.setValue(myProfile.value!!.asFirebaseModel(), createId.value!!)

                albumRepostiery.setValue(firebaseAlbum, createId.value!!) { key ->
                    _albumKey.value = key
                }


            } catch (error: IOException) {
                error.printStackTrace()
            } catch (error: NullPointerException) {
                error.printStackTrace()
            }

        }
    }

    /**
     * ????????? Album??? ?????? ??????, AlbumKey??? ??????
     * ??? Participants ?????? RequestAlbum??? ????????? ??????.
     * AlbumRepositery.requestAlbum()???????????? ?????????
     * */
    fun createRequestAlbum(albumKey: String) {
        viewModelScope.launch {

            if (!participants.value.isNullOrEmpty()) {
                val thumbnail = BitmapUtils.bitmapToString(createThumbnail!!)

                val requestAlbum = FirebaseRequestAlbum(
                    id = createId.value!!,
                    name = createName.value!!,
                    thumbnail = thumbnail,
                    participants = createParticipants.value!!,
                    albumKey = albumKey,
                    ""
                )

                withContext(Dispatchers.IO) {
                    albumRequestRepositery.setValueToParticipants(
                        participants.value!!,
                        requestAlbum
                    )
                }

                withContext(Dispatchers.Main) {
                    navigateToAlbum()
                }
            }
        }

    }

    /**
     * viewModel - participants ??????????????? ?????? ???????????? ?????????
     * */
    fun setParticipants(list: List<Friend>) {
        _participants.value = list

    }

    /**
     * viewModel - createThumbnail ??????????????? ?????? ???????????? ?????????
     * */
    fun setThumbnail(bitmap: Bitmap) {
        createThumbnail = bitmap
    }

    /**
     * ????????? ???????????? ????????? ??????,
     * ?????? create ???????????? ????????? ?????? "" ?????? null ??? ???????????????.
     * */
    fun setNullCreateProperty() {
        _participants.value = listOf()
        createThumbnail = null
    }

    /**
     * AlbumCreateart ????????????????????? "??????" ????????? ?????????
     * ViewModel - participants ??????????????? ?????? null ????????? (?????? ????????? ???????????? ?????????)
     * listOf() ??? ??????. ????????? ????????? ?????????
     * */
    fun checkParticipants() {
        if (_participants.value == null) {
            _participants.value = listOf()
        }
    }


    fun removeListener() {
        albumRequestRepositery.removeListener(myProfile.value!!.friendCode.drop(1))
        albumRepostiery.removeListener()
    }

    //navigate
    fun navigateToAlbum() {
        _naviToAlbum.value = Event(Unit)
    }

}