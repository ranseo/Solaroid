package com.example.solaroid.ui.home.fragment.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.room.Database
import com.example.solaroid.Event
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.album.HomeAlbumRepositery
import com.example.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

enum class PhotoTicketFilter(val filter: String) {
    DESC(filter = "DESC"),
    ASC(filter = "ASC"),
    FAVORTIE(filter = "FAVORITE");

    companion object {
        fun convertStringToFilter(filter: String): PhotoTicketFilter {
            return when (filter) {
                "DESC" -> DESC
                "ASC" -> ASC
                "FAVORITE" -> FAVORTIE
                else -> throw IllegalArgumentException("UNDEFINED_STATUS")
            }
        }
    }
}

//AlbumId, AlbumKey
typealias AIAK = Pair<String,String>
class HomeGalleryViewModel(dataSource: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private val albumRepositery = AlbumRepositery(database,fbAuth, fbDatabase, AlbumDataSource())
    private val homeAlbumRepositery = HomeAlbumRepositery(database)


    val photoTicketRepositery = PhotoTicketRepositery(
        database, fbAuth, fbDatabase, fbStorage,
        PhotoTicketListenerDataSource()
    )

    var albumId = homeAlbumRepositery.homeAlbumId

    private val _album = MutableLiveData<DatabaseAlbum>()
    val album : LiveData<DatabaseAlbum>
        get() = _album

//    val homeAlbumKey = T
//

    private val _photoTicketsSetting = MutableLiveData<Event<List<PhotoTicket>>>()
    val photoTicketSetting: LiveData<Event<List<PhotoTicket>>>
        get() = _photoTicketsSetting


    private val _naviToFrame = MutableLiveData<Event<PhotoTicket>>()
    val naviToFrame: LiveData<Event<PhotoTicket>>
        get() = _naviToFrame

    private val _naviToAdd = MutableLiveData<Event<AIAK>>()
    val naviToAdd: LiveData<Event<AIAK>>
        get() = _naviToAdd

    private val _naviToCreate = MutableLiveData<Event<AIAK>>()
    val naviToCreate: LiveData<Event<AIAK>>
        get() = _naviToCreate

    private val _naviToAlbum = MutableLiveData<Event<Any?>>()
    val naviToAlbum : LiveData<Event<Any?>>
        get() = _naviToAlbum

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

    }



    fun refreshFirebaseListener(albumId:String, albumKey:String) {
        viewModelScope.launch {
            try {
                val user = fbAuth.currentUser!!
                photoTicketRepositery.refreshPhotoTickets(albumId,albumKey) { firebasePhotoTickets ->
                    viewModelScope.launch(Dispatchers.Default) {
                        Log.i(TAG,"viewModelScope.launch(Dispatchers.IO) : ${this}")
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
        val albumId = album.value?.id
        val albumKey = album.value?.key

        if(albumId != null && albumKey != null)
        _naviToAdd.value = Event(AIAK(albumId, albumKey))
    }

    fun navigateToCreate() {
        val albumId = album.value?.id
        val albumKey = album.value?.key

        if(albumId != null && albumKey != null)
        _naviToCreate.value = Event(AIAK(albumId,albumKey))
    }

    fun navigateToAlbum() {
        _naviToAlbum.value = Event(Unit)
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
        val albumId = album.value?.id
        val albumKey = album.value?.key

        if(albumId != null && albumKey != null)
        photoTicketRepositery.removeListener(albumId, albumKey)
    }

    fun setAlbum(albumId: String) {
        viewModelScope.launch {
            val tmp = albumRepositery.getAlbum(albumId)
            //Log.i(TAG,"tmp : ${tmp}")
            _album.value =  tmp
        }
    }


    companion object {
        const val TAG = "갤러리_뷰모델"

    }


}