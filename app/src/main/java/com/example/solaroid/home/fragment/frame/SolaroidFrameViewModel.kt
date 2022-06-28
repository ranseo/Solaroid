package com.example.solaroid.home.fragment.frame

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.home.fragment.gallery.PhotoTicketFilter
import com.example.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlin.Exception


class SolaroidFrameViewModel(
    dataSource: DatabasePhotoTicketDao,
    application: Application,
    filter: PhotoTicketFilter
) :
    AndroidViewModel(application) {


    companion object {
        const val TAG = "프레임뷰모델"
    }

    val database = dataSource

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private val photoTicketRepositery: PhotoTicketRepositery =
        PhotoTicketRepositery(
            dataSource,
            fbAuth,
            fbDatabase,
            fbStorage,
            PhotoTicketListenerDataSource()
        )

    val photoTickets = when (filter) {
        PhotoTicketFilter.DESC -> {
            photoTicketRepositery.photoTicketsOrderByDesc
        }
        PhotoTicketFilter.ASC -> {
            photoTicketRepositery.photoTicketsOrderByAsc
        }
        PhotoTicketFilter.FAVORTIE -> {
            photoTicketRepositery.photoTicketsOrderByFavorite
        }
    }


//    var initPhotoTicket : PhotoTicket? = null


    private val _startPosition = MutableLiveData<Int>()
    val startPosition: LiveData<Int>
        get() = _startPosition


    /**
     * Room으로 부터 얻은 포토티켓 리스트의 사이즈를 갖는 프로퍼티.
     * */
    private val _photoTicketsSize = MutableLiveData<Int>()
    val photoTicketsSize: LiveData<Int>
        get() = _photoTicketsSize

    fun setPhotoTicketSize(size: Int) {
        _photoTicketsSize.value = size
    }

    /**
     * 현재 viewPager에서 사용자가 보고 있는 포토티켓의 위치를 나타내는 프로퍼티.
     * 해당 변수의 값을 이용하여 현재 사용자가 보고 있는 포토티켓의 값을 설정할 수 있다.
     * */
    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int>
        get() = _currentPosition


    /**
     * 현재 viewPager에서 사용자가 보고 있는 페이지에 속한 포토티켓
     * */
    val currPhotoTicket: LiveData<PhotoTicket?> =
        Transformations.map(_currentPosition) { position ->
            if (position >= 0) {
                val list = photoTickets.value

                if (!list.isNullOrEmpty()) {
                    list[position]
                } else null
            } else null
        }


    /**
     * 현재 viewPager에서 사용자가 보고 있는 페이지에 속한 포토티켓의 즐겨찾기 상태를 나타내는 프로퍼티
     * 해당 값을 이용하여 하단 네비게이션의 즐겨찾기 icon의 drawable 이미지를 바꾼다.
     * */
    private val _favorite = MutableLiveData<Boolean?>()
    val favorite: LiveData<Boolean?>
        get() = _favorite


    init {
        Log.i(TAG, "뷰모델 Init()")
    }


    /**
     * 현재 포토티켓의 즐겨찾기 상태를 대입한다.
     * */
    fun setCurrentFavorite(favorite: Boolean) {
        Log.i(TAG, "setCurrentFavorite : ${favorite}")
        _favorite.value = favorite
    }


    /**
     * currentPosition의 값 설정.
     * */
    fun setCurrentPosition(position: Int) {
        Log.i(TAG, "setCurrentPosition : ${position}")
        _currentPosition.value = position
    }


    fun refreshPhotoTicket(photoKey: String) {
        viewModelScope.launch {
            var deferred: Deferred<PhotoTicket> = async{
                photoTicketRepositery.getPhotoTicket(photoKey)
            }


            launch(Dispatchers.Default) {
                val photoTicket= deferred.await()
                val idx = photoTickets.value?.indexOf(photoTicket) ?: 0

                withContext(Dispatchers.Main) {
                    _startPosition.value = idx
                }
            }
            Log.i(TAG, "startPosition.value  :  ${startPosition.value}")
        }
    }

    fun refreshPhotoTicket() {
        viewModelScope.launch(Dispatchers.Default) {
            val idx = photoTickets.value?.indexOf(currPhotoTicket.value) ?: 0
            withContext(Dispatchers.Main) {
                _startPosition.value = idx
            }
        }
    }

    /**
     * 포토티켓 즐겨찾기를 등록하거나 해제할 경우, 새로운 즐겨찾기 값으로 해당 포토티켓을 update한다.
     * */
    fun updatePhotoTicketFavorite() {
        viewModelScope.launch {
            currPhotoTicket.value?.let {
                it.favorite = it.favorite != true
                Log.i(TAG, "updatePhotoTicketFavorite() : ${it.favorite}")
                photoTicketRepositery.updatePhotoTickets(it, getApplication())
            }
        }
    }

    /**
     * 포토티켓을 삭제.
     * */
    fun deletePhotoTicket(key: String) {
        viewModelScope.launch {
            photoTicketRepositery.deletePhotoTickets(key, getApplication())
        }
    }


    //SolaroidCreateFragment로 이동
    private val _naviToCreateFrag = MutableLiveData<Event<Boolean>>()
    val naviToCreateFrag: LiveData<Event<Boolean>>
        get() = _naviToCreateFrag


    //SolaroidEditFragment로 이동
    private val _naviToEditFrag = MutableLiveData<Event<String>>()
    val naviToEditFrag: LiveData<Event<String>>
        get() = _naviToEditFrag


    /**
     * trigger for navigate to SolaroidAddFragment
     * */
    private val _naviToAddFrag = MutableLiveData<Event<Boolean>>()
    val naviToAddFrag: LiveData<Event<Boolean>>
        get() = _naviToAddFrag


    fun navigateToCreate() {
        _naviToCreateFrag.value = Event(true)
    }

    fun navigateToEdit(key: String) {
        _naviToEditFrag.value = Event(key)
    }

    fun navigateToAdd() {
        _naviToAddFrag.value = Event(true)
    }

    /////////////////////////////////////////////////////////////////


}