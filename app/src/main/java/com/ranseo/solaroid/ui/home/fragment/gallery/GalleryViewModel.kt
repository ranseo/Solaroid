package com.ranseo.solaroid.ui.home.fragment.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.Event
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.repositery.phototicket.GetPhotoTicketWithAlbumRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ranseo.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.ranseo.solaroid.repositery.phototicket.PhotoTicketRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GalleryViewModel(
    dataSource: DatabasePhotoTicketDao,
    application: Application,
    _albumId: String
) :
    AndroidViewModel(application) {

    private val database = dataSource
    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private val getPhotoTicketWithAlbumRepositery = GetPhotoTicketWithAlbumRepositery(
        database, fbAuth, _albumId
    )

    val photoTicketRepositery = PhotoTicketRepositery(
        database, fbAuth, fbDatabase, fbStorage,
        PhotoTicketListenerDataSource()
    )

    private val _photoTicketsSetting = MutableLiveData<Event<List<PhotoTicket>>>()
    val photoTicketSetting: LiveData<Event<List<PhotoTicket>>>
        get() = _photoTicketsSetting


    private val _naviToFrame = MutableLiveData<Event<PhotoTicket>>()
    val naviToFrame: LiveData<Event<PhotoTicket>>
        get() = _naviToFrame

    private val _naviToAdd = MutableLiveData<Event<Any?>>()
    val naviToAdd: LiveData<Event<Any?>>
        get() = _naviToAdd

    private val _naviToCreate = MutableLiveData<Event<Any?>>()
    val naviToCreate: LiveData<Event<Any?>>
        get() = _naviToCreate

    private val _naviToHome = MutableLiveData<Event<Any?>>()
    val naviToHome: LiveData<Event<Any?>>
        get() = _naviToHome


    private val _filter = MutableLiveData(PhotoTicketFilter.DESC)
    val filter: LiveData<PhotoTicketFilter>
        get() = _filter


    val photoTickets = Transformations.switchMap(filter) { filter ->
        Log.i(TAG, "val photoTickets = Transformations.map(filter) { filter -> ${filter}")
        getPhotoTickets(filter)
    }

    private fun getPhotoTickets(filter: PhotoTicketFilter): LiveData<List<PhotoTicket>> {
        return when (filter) {
            PhotoTicketFilter.DESC -> {
                Log.i(TAG, "photoTicketsOrderByDesc.value")
                getPhotoTicketWithAlbumRepositery.photoTicketsOrderByDesc
            }
            PhotoTicketFilter.ASC -> {
                Log.i(TAG, "photoTicketsOrderByAsc")
                getPhotoTicketWithAlbumRepositery.photoTicketsOrderByAsc
            }
            PhotoTicketFilter.FAVORTIE -> {
                Log.i(TAG, "photoTicketsOrderByFavorite")
                getPhotoTicketWithAlbumRepositery.photoTicketsOrderByFavorite
            }
        }
    }

    private val _photoTicketState = MutableLiveData<PhotoTicketState>()
    val photoTicketState : LiveData<PhotoTicketState>
        get() = _photoTicketState


    private val _photoDeleteList = mutableListOf<PhotoTicket>()
    val photoDeleteList: List<PhotoTicket>
        get() = _photoDeleteList

    init {
        Log.i(TAG, "albumId: ${_albumId}")
    }

    /**
     *  DeleteList에 삭제할 photoTicket을 추가하거나 삭제.
     * */
    fun addOrRemoveDeleteList(photoTicket: PhotoTicket) {
        val idx = photoDeleteList.indexOf(photoTicket)
        if (idx > -1) _photoDeleteList.removeAt(idx)
        else _photoDeleteList.add(photoTicket)
    }

    private fun clearDeleteList() {
        _photoDeleteList.clear()
    }


    /**
     * 포토티켓을 삭제.
     * */
    fun deletePhotoTickets() {
        viewModelScope.launch(Dispatchers.Default) {
            for (photoTicket in photoDeleteList) {
                val (albumId, albumKey) = photoTicket.albumInfo
                val key = photoTicket.id
                photoTicketRepositery.deletePhotoTicket(
                    albumId, albumKey, key, getApplication()
                )

                photoTicketRepositery.deletePhotoTicketInRoom(key)
            }

            clearDeleteList()
        }
    }

    fun changePhotoTicketState() {
        when (photoTicketState.value) {
            PhotoTicketState.NORMAL -> {
                _photoTicketState.value = PhotoTicketState.LONG
            }
            PhotoTicketState.LONG -> {
                _photoTicketState.value = PhotoTicketState.NORMAL
            }
        }
    }
    fun setFilter(filter: String) {
        _filter.value = PhotoTicketFilter.convertStringToFilter(filter)
    }

    fun navigateToFrame(photoTicket: PhotoTicket) {
        _naviToFrame.value = Event(photoTicket)
    }

    fun navigateToAdd() {
        _naviToAdd.value = Event(Unit)
    }

    fun navigateToCreate() {
        _naviToCreate.value = Event(Unit)
    }

    fun navigateToHome() {
        _naviToHome.value = Event(Unit)
    }


    companion object {
        const val TAG = "갤러리_뷰모델"
    }


}