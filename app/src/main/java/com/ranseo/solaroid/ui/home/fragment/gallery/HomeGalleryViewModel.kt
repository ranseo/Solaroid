package com.ranseo.solaroid.ui.home.fragment.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.ranseo.solaroid.Event
import com.ranseo.solaroid.datasource.album.AlbumDataSource
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.firebase.FirebasePhotoTicket
import com.ranseo.solaroid.firebase.asDatabaseModel
import com.ranseo.solaroid.parseAlbumIdDomainToFirebase
import com.ranseo.solaroid.repositery.album.AlbumRepositery
import com.ranseo.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException


/**
 * PhotoTicket
 * */
enum class PhotoTicketState {
    NORMAL,
    LONG
}

/**
 * PhotoTicket을 즐겨찾기 전용 또는 날짜순(오름차순, 내림차순) 으로 정렬하기 위해
 * enum class 를 만들어 활용.
 * */
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

class HomeGalleryViewModel(dataSource: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private val albumRepositery = AlbumRepositery(database, fbAuth, fbDatabase, AlbumDataSource())

    val photoTicketRepositery = PhotoTicketRepositery(
        database, fbAuth, fbDatabase, fbStorage,
        PhotoTicketListenerDataSource()
    )

    private val _naviToFrame = MutableLiveData<Event<PhotoTicket>>()
    val naviToFrame: LiveData<Event<PhotoTicket>>
        get() = _naviToFrame

    private val _naviToAdd = MutableLiveData<Event<Any?>>()
    val naviToAdd: LiveData<Event<Any?>>
        get() = _naviToAdd

    private val _naviToCreate = MutableLiveData<Event<Any?>>()
    val naviToCreate: LiveData<Event<Any?>>
        get() = _naviToCreate

    private val _naviToAlbum = MutableLiveData<Event<Any?>>()
    val naviToAlbum: LiveData<Event<Any?>>
        get() = _naviToAlbum

    private val _filter = MutableLiveData(PhotoTicketFilter.DESC)
    val filter: LiveData<PhotoTicketFilter>
        get() = _filter

    private val _photoTicketState = MutableLiveData<PhotoTicketState>(PhotoTicketState.NORMAL)
    val photoTicketState: LiveData<PhotoTicketState>
        get() = _photoTicketState

//    private val _albums = MutableLiveData<List<DatabaseAlbum>>()
//    val albums: LiveData<List<DatabaseAlbum>>
//        get() = _albums

    val albums = albumRepositery.albums

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
        //refreshAlbumList()
    }

//    private fun refreshAlbumList() {
//        viewModelScope.launch {
//            albumRepositery.addSingleValueEventListener { albums ->
//                _albums.value = albums
//            }
//        }
//    }


    fun refreshFirebaseListener(albumId: String, albumKey: String) {
        viewModelScope.launch {
            try {
                val user = fbAuth.currentUser!!
                Log.i(
                    TAG,
                    "refreshFirebaseListener : albumId : ${albumId}, albumKey :  ${albumKey}"
                )
                photoTicketRepositery.refreshPhotoTickets(
                    albumId,
                    albumKey
                ) { firebasePhotoTickets ->
                    viewModelScope.launch(Dispatchers.Default) {
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

    fun refreshPhtoTicketState() {
        _photoTicketState.value = PhotoTicketState.NORMAL
    }


    /**
     * 포토티켓을 삭제.
     * */
    fun deletePhotoTicket(photoTicket: PhotoTicket) {
        viewModelScope.launch {
            val (albumId, albumKey) = photoTicket.albumInfo
            val key = photoTicket.id
            photoTicketRepositery.deletePhotoTicket(
                albumId, albumKey, key, getApplication()
            )

            photoTicketRepositery.deletePhotoTicketInRoom(key)


        }
    }


    fun navigateToFrame(photoTicket: PhotoTicket) {
        Log.i(TAG, "navigateToFrame : $photoTicket")
        _naviToFrame.value = Event(photoTicket)
    }

    fun navigateToAdd() {
        _naviToAdd.value = Event(Unit)
    }

    fun navigateToCreate() {
        _naviToCreate.value = Event(Unit)
    }

    fun navigateToAlbum() {
        _naviToAlbum.value = Event(Unit)
    }


    fun removeListener() {
        try {
            for (a in albums.value!!)
                photoTicketRepositery.removeListener(
                    parseAlbumIdDomainToFirebase(a.id, a.key),
                    a.key
                )
        } catch (error: NullPointerException) {
            error.printStackTrace()
        } catch (error: IOException) {
            error.printStackTrace()
        } catch (error: IndexOutOfBoundsException) {
            error.printStackTrace()
        }
    }


    companion object {
        const val TAG = "홈_갤러리_뷰모델"

    }


}