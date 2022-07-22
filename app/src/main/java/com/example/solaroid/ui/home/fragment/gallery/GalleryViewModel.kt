package com.example.solaroid.ui.home.fragment.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*


class GalleryViewModel(
    dataSource: DatabasePhotoTicketDao,
    application: Application,
    _albumId: String,
    _albumKey: String
) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    val photoTicketRepositery = PhotoTicketRepositery(
        database, fbAuth, fbDatabase, fbStorage,
        PhotoTicketListenerDataSource()
    )

    val albumId = _albumId
    val albumKey = _albumKey


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
                photoTicketRepositery.photoTicketsOrderByDesc
            }
            PhotoTicketFilter.ASC -> {
                Log.i(TAG, "photoTicketsOrderByAsc")
                photoTicketRepositery.photoTicketsOrderByAsc
            }
            PhotoTicketFilter.FAVORTIE -> {
                Log.i(TAG, "photoTicketsOrderByFavorite")
                photoTicketRepositery.photoTicketsOrderByFavorite
            }
        }
    }

    init {
        refreshFirebaseListener(_albumId)
    }

    fun refreshFirebaseListener(albumId: String) {
        viewModelScope.launch {
            try {
                val user = fbAuth.currentUser!!
                photoTicketRepositery.refreshPhotoTickets(albumId, albumKey) { firebasePhotoTickets ->
                    viewModelScope.launch(Dispatchers.Default) {
                        Log.i(TAG, "viewModelScope.launch(Dispatchers.IO) : ${this}")
                        insert(firebasePhotoTickets, user.email!!)
                    }
                }
            } catch (error: Exception) {
                Log.d(TAG, "error : ${error.message}")
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

    suspend fun insert(firebasePhotoTickets: List<FirebasePhotoTicket>, user: String) =
        coroutineScope {

            val deferred = async {
                firebasePhotoTickets.map {
                    it.asDatabaseModel(user)
                }
            }

            withContext(Dispatchers.IO) {
                val databasePhotoTickets = deferred.await()
                database.insertAll(databasePhotoTickets)
            }

        }


    fun removeListener() {
        photoTicketRepositery.removeListener(albumId, albumKey)
    }


    companion object {
        const val TAG = "갤러리_뷰모델"

    }


}