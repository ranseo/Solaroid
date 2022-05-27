package com.example.solaroid.solaroidframe

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.lang.Exception

enum class PhotoTicketFilter {
    LATELY,
    FAVORITE
}

class SolaroidFrameViewModel(dataSource: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {


    companion object {
        const val TAG = "프레임뷰모델"
    }

    val database = dataSource

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()

    private var valueEventListener : ValueEventListener? = null

    private val photoTicketRepositery: PhotoTicketRepositery =
        PhotoTicketRepositery(dataSource, fbAuth, fbDatabase, fbStorage)
    val photoTicketsOrderByLately: LiveData<List<PhotoTicket>> =
        photoTicketRepositery.photoTicketsOrderByLately
    val photoTicketsOrderByFavorite: LiveData<List<PhotoTicket>> =
        photoTicketRepositery.photoTicketsOrderByFavorte

    private val _photoTickets = MutableLiveData<Event<List<PhotoTicket>>>()
    val photoTickets: LiveData<Event<List<PhotoTicket>>>
        get() = _photoTickets


    private val _photoTicketFilter = MutableLiveData(PhotoTicketFilter.LATELY)
    val photoTicketFilter: LiveData<PhotoTicketFilter>
        get() = _photoTicketFilter


    /**
     * Room으로 부터 얻은 포토티켓 리스트의 사이즈를 갖는 프로퍼티.
     * */
    private val _photoTicketsSize = MutableLiveData<Int>()
    val photoTicketsSize : LiveData<Int>
        get() = _photoTicketsSize

    fun setPhotoTicketSize(size :Int) {
        _photoTicketsSize.value = size
    }

    /**
     * 현재 포토티켓의 정렬 방식(최신순 또는 즐겨찾기)에 따라서 UI에 TEXT 표시
     * */
    private val _currFilterText = MutableLiveData("최신순")
    val currFilterText: LiveData<String>
        get() = _currFilterText


    /**
     * 현재 viewPager에서 사용자가 보고 있는 페이지에 속한 포토티켓의 즐겨찾기 상태를 나타내는 프로퍼티
     * 해당 값을 이용하여 하단 네비게이션의 즐겨찾기 icon의 drawable 이미지를 바꾼다.
     * */
    private val _favorite = MutableLiveData<Boolean?>()
    val favorite: LiveData<Boolean?>
        get() = _favorite

    /**
     * 현재 포토티켓의 즐겨찾기 상태를 대입한다.
     * */
    fun setCurrentFavorite(favorite: Boolean) {
        _favorite.value = favorite
    }

    //viewPager2의 각 page 위치에 배치된 item (=DatabasePhotoTicket)을 할당.
//    private val _DatabasePhotoTicket = MutableLiveData<DatabasePhotoTicket?>()
//    val DatabasePhotoTicket: LiveData<DatabasePhotoTicket?>
//        get() = _DatabasePhotoTicket
//

    /**
     * 오른쪽 상단의 오버플로우 메뉴 클릭 시, popup Menu open/close 하는 프로퍼티
     * */
    private val _popUpMenu = MutableLiveData<Event<Boolean>>()
    val popUpMenu: LiveData<Event<Boolean>>
        get() = _popUpMenu


    /**
     * 현재 viewPager에서 사용자가 보고 있는 포토티켓의 위치를 나타내는 프로퍼티.
     * 해당 변수의 값을 이용하여 현재 사용자가 보고 있는 포토티켓의 값을 설정할 수 있다.
     * */
    private val _currentPosition = MutableLiveData(-1)
    val currentPosition: LiveData<Int>
        get() = _currentPosition

    /**
     * currentPosition의 값 설정.
     * */
    fun setCurrentPosition(position: Int) {
        Log.i(TAG,"setCurrentPosition : ${position}")
        _currentPosition.value = position
    }

    /**
     * 현재 viewPager에서 사용자가 보고 있는 페이지에 속한 포토티켓
     * */
    val currPhotoTicket: LiveData<PhotoTicket?> = Transformations.map(currentPosition) { position ->
        if (position >= 0) {
            val list = when(photoTicketFilter.value) {
                PhotoTicketFilter.LATELY -> {
                    photoTicketsOrderByLately.value
                }
                else -> {
                    photoTicketsOrderByFavorite.value
                }
            }

            Log.i(TAG, "Position : ${position}")
            if (!list.isNullOrEmpty()) {
                list[position]
            } else null
        } else null
    }


    init {
        Log.d(TAG, "Init")

        refreshDataFromRepositery()

        // 현재 사용자가 보고 있는 포토티켓을 설정하기 위해서 currentPosition 값을
        // photoTickets의 인덱스로 이용하여 photoTicket을 가져온다.
        // 만약 현재 포지션이 0보다 작다면 아무런 포토티켓이 없는 상태이므로 null값을 대입.
        // 그렇지 않은 경우, 만약 photoTickets이 비어있다면 null 값을 대입.
        // 비어있지 않은 경우 현재 포지션을 idx로 사용하여 PhotoTicket 타입의 원소를 대입한다.


//        currPhotoTicket = Transformations.map(currentPosition) { position ->
//            if (position >= 0) {
//                val list = photoTickets.value?.getContentIfNotHandled()
//                if (list != null) {
//                    list[position]
//                } else null
//            } else null
//        }


    }

    /**
     * PhotoTicketRepositery를 이용하여 firebase realtime database로 부터 Room Database에 넣을 photoTicket Model 을 get.
     * */
    fun refreshDataFromRepositery() {
        viewModelScope.launch {
            try {
                valueEventListener = photoTicketRepositery.refreshPhotoTickets(application = getApplication())
                if(valueEventListener == null) Log.i(TAG, "firebase valueEventListener error")
                else fbDatabase.reference.child("photoTicket").child(fbAuth.currentUser!!.uid).addListenerForSingleValueEvent(
                    valueEventListener!!
                )

            } catch (error: Exception) {
                Log.i(TAG,"firebase error : ${error.message}")
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


    /**
     * start, insert, update, delete 등 포토티켓 리스트에 변화가 있을 때 마다 photoTickets를 refresh하는 함수,
     */
    fun refreshPhotoTicketEvent() {
        sortPhotoTicketsByFilter()
    }

    /**
     * photoTicketFilter의 값을 설정.
     * */
    fun setPhotoTicketFilter(filter: PhotoTicketFilter) {
        _photoTicketFilter.value = filter
    }


    /**
     * 수정예정.
     * 프레임컨테이너 프래그먼트 내 fragmentContainer 내에 (즐겨찾기 <-> 최신순) 프래그먼트를 전환할 수 있다.
     * */
    private fun sortPhotoTicketsByFilter() {
        when (photoTicketFilter.value) {
            PhotoTicketFilter.LATELY -> {
                _currFilterText.value = "최신순"
                val value = photoTicketsOrderByLately.value
                value?.let {
                    _photoTickets.value = Event(it)
                }
            }
            PhotoTicketFilter.FAVORITE -> {
                _currFilterText.value = "즐겨찾기"
                val value = photoTicketsOrderByFavorite.value
                value?.let {
                    _photoTickets.value = Event(it)
                }
            }
            else -> {}
        }
    }


    fun onFilterPopupMenu() {
        _popUpMenu.value = Event(true)
    }


    //    네비게이션 변수 및 함수.
    private val _naviToDetailFrag = MutableLiveData<Event<String>>()
    val naviToDetailFrag: LiveData<Event<String>>
        get() = _naviToDetailFrag

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


    //갤러리 프래그먼트로 이동
    private val _naviToGallery = MutableLiveData<Event<Boolean>>()
    val naviToGallery: LiveData<Event<Boolean>>
        get() = _naviToGallery


    fun navigateToDetail(DatabasePhotoTicketKey: String) {
        _naviToDetailFrag.value = Event(DatabasePhotoTicketKey)
    }

    fun navigateToCreate() {
        _naviToCreateFrag.value = Event(true)
    }

    fun navigateToEdit(key: String) {
         _naviToEditFrag.value = Event(key)
    }

    fun navigateToAdd() {
        _naviToAddFrag.value = Event(true)
    }

    fun navigateToGallery() {
        _naviToGallery.value = Event(true)
    }


    /////////////////////////////////////////////////////////////////


    //Firebase
    fun logout() {
        fbAuth.signOut()
    }

    override fun onCleared() {
        if(valueEventListener!=null) fbDatabase.reference.removeEventListener(valueEventListener!!)
        super.onCleared()
    }
}