package com.example.solaroid.solaroidframe

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

enum class PhotoTicketFilter {
    LATELY,
    FAVORITE
}

class SolaroidFrameViewModel(dataSource: PhotoTicketDao, application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "프레임뷰모델"
    }

    val database = dataSource


    private var photoTicketFilter = PhotoTicketFilter.LATELY


    //adapter에 item으로 전달될 LiveData
    var photoTickets = database.getAllPhotoTicket()

    private val _photoTicketsByFirebase = MutableLiveData<List<PhotoTicket>>()
    val photoTicketsByFirebase : LiveData<List<PhotoTicket>>
        get() = _photoTicketsByFirebase

    //테스트
    fun setPhotoTicketsByFirebase(list: List<PhotoTicket>) {
        viewModelScope.launch {
            insertAll(list)
        }
    }


    var preSize = 0
    var photoTicketsSize = Transformations.map(photoTickets) {
        it?.let {
            it.size
        }
    }

    private val _currFilterText = MutableLiveData<String>("최신순")
    val currFilterText : LiveData<String>
        get() = _currFilterText

    private val _favorite = MutableLiveData<Boolean?>()
    val favorite: LiveData<Boolean?>
        get() = _favorite

    //viewPager2의 각 page 위치에 배치된 item (=PhotoTicket)을 할당.
//    private val _photoTicket = MutableLiveData<PhotoTicket?>()
//    val photoTicket: LiveData<PhotoTicket?>
//        get() = _photoTicket
//
    val photoTicket : LiveData<PhotoTicket?>

    //버튼 클릭 시, popup Menu
    private val _popUpMenu = MutableLiveData<Boolean>(false)
    val popUpMenu: LiveData<Boolean>
        get() = _popUpMenu

    //spin_image
    private val _imageSpin = MutableLiveData(false)
    val imageSpin: LiveData<Boolean>
        get() = _imageSpin


    private val _naviToDetailFrag = MutableLiveData<Long?>()
    val naviToDetailFrag: LiveData<Long?>
        get() = _naviToDetailFrag

    //SolaroidCreateFragment로 이동
    private val _naviToCreateFrag = MutableLiveData<Boolean>(false)
    val naviToCreateFrag: LiveData<Boolean>
        get() = _naviToCreateFrag


    //SolaroidEditFragment로 이동
    private val _naviToEditFrag = MutableLiveData<Long?>()
    val naviToEditFrag: LiveData<Long?>
        get() = _naviToEditFrag

    /**
     * trigger for navigate to SolaroidAddFragment
     * */
    private val _naviToAddFrag = MutableLiveData<Boolean>()
    val naviToAddFrag: LiveData<Boolean>
        get() = _naviToAddFrag


    //최신순 프래그먼트로 이동.
    private val _naviToLately = MutableLiveData<Boolean>(true)
    val naviToLately: LiveData<Boolean>
        get() = _naviToLately


    //즐겨찾기 프래그먼트로 이동.
    private val _naviToFavorite = MutableLiveData<Boolean>(false)
    val naviToFavorite: LiveData<Boolean>
        get() = _naviToFavorite

    //갤러리 프래그먼트로 이동
    private val _naviToGallery = MutableLiveData<Boolean>(false)
    val naviToGallery: LiveData<Boolean>
        get() = _naviToGallery


    //즐겨찾기 해재 시, 해당 viewPager의 position을 기록 -> 이는 viewPager의 onPageSelected의 문제점을 해결하기 위한 변수
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int>
        get() = _currentPosition

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    init {
        Log.d(TAG, "Init")
        photoTicket= Transformations.map(currentPosition) { position ->
            if(position>=0) {
                val list = photoTickets.value
                if(list!=null) {
                    list[position]
                } else null
            } else null
        }
    }


//    fun setCurrentPhotoTicket(curr: PhotoTicket?) {
//        _photoTicket.value = curr
//    }


    fun setCurrentFavorite(favorite: Boolean) {
        _favorite.value = favorite
    }




    //favorite(즐겨찾기) 값에 따라서 photoTicket를 업데이트하고, 현재 photoTicket값 갱신
    fun togglePhotoTicketFavorite(favorite: Boolean) {
        viewModelScope.launch {
            photoTicket.value?.let {
                it.favorite = favorite
                update(it)
                Log.d(
                    "FrameFragment",
                    "togglePhotoTicketFavorite photoTicket Id ${it.id} : ${favorite}"
                )
            }
        }
    }

//    fun offPhotoTicketFavorite(favorite: Boolean) {
//        viewModelScope.launch {
//            _photoTicket.value?.let {
//                it.favorite = favorite
//                update(it)
//                Log.d("프레임프래그먼트", "togglePhotoTicket ${photoTicket.value?.id} : ${favorite}")
//            }
//        }
//    }


    fun sortByFilter(filter: PhotoTicketFilter) {
        photoTickets = when (filter) {
            PhotoTicketFilter.LATELY -> {
                _currFilterText.value = "최신순"
                Log.d("sortByFilter", "LATELY SUCCESS")
                database.getAllPhotoTicket()
            }
            PhotoTicketFilter.FAVORITE -> {
                _currFilterText.value = "즐겨찾기"
                Log.d("sortByFilter", "FAVORITE SUCCESS")
                database.getFavoritePhotoTicket(true)
            }
        }
        photoTicketsSize = Transformations.map(photoTickets) {
            it?.let {
                it.size
            }
        }
    }

    //
    fun onFilterPopupMenu() {
        _popUpMenu.value = true
    }

    fun doneFilterPopupMenu() {
        _popUpMenu.value = false
    }


    //포토티켓 필터 set
    fun setPhotoTicketFilter(filter: PhotoTicketFilter) {
        photoTicketFilter = filter
        Log.d("setPhotoTicketFilter", "${photoTicketFilter}")
    }


//    네비게이션 함수.

    fun navigateToDetail(photoTicketKey: Long) {
        _naviToDetailFrag.value = photoTicketKey
    }

    fun doneNavigateToDetail() {
        _naviToDetailFrag.value = null
    }

    fun navigateToLately(navi: Boolean) {
        _naviToLately.value = navi
    }

    fun doneNavigateToLately() {
        _naviToLately.value = false
    }

    fun navigateToFavorite(navi: Boolean) {
        _naviToFavorite.value = navi
    }

    fun doneNavigateToFavorite() {
        _naviToFavorite.value = false
    }

    fun navigateToCreate() {
        _naviToCreateFrag.value = true
    }

    fun doneNavigateToCreate() {
        _naviToCreateFrag.value = false
    }

    fun navigateToEdit(photoTicketKey: Long?) {
        photoTicketKey?.let {
            _naviToEditFrag.value = photoTicketKey
        }
    }

    fun doneNavigateToEdit() {
        _naviToEditFrag.value = null
    }

    fun navigateToAdd() {
        _naviToAddFrag.value = true
    }

    fun doneNavigateToAdd() {
        _naviToAddFrag.value = false
    }

    fun navigateToGallery() {
        _naviToGallery.value = true
    }

    fun doneNavigateToGallery() {
        _naviToGallery.value = false
    }


    /////////////////////////////////////////////////////////////////






    suspend fun getPhotoTicket(key: Long): PhotoTicket = database.getPhotoTicket(key)

    //이미지 회전
    fun spinImage() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }


    //onClick

    fun onListItemClick() {
        spinImage()
    }


    fun deletePhotoTicket(key: Long) {
        viewModelScope.launch {
            delete(key)
        }
    }

    //dialog 관련

    /**
     * ListDialongFrament를 생성하고 show()하는 showListDialog() 함수를 호출하게 만드는
     * 변수
     * */
//    private val _listDialog = MutableLiveData<Boolean>(false)
//    val listDialog : LiveData<Boolean>
//        get() = _listDialog
//
//    fun toggleListDialog() {
//        val toggle = listDialog.value!!
//        _listDialog.value = !toggle
//    }

    //Firebase
    fun logout() {
        AuthUI.getInstance()
            .signOut(getApplication())
    }


    //Database

    private suspend fun update(photoTicket: PhotoTicket) {
        database.update(photoTicket)
    }

    private suspend fun delete(key: Long) {
        database.delete(key)
    }

    private suspend fun insertAll(list: List<PhotoTicket>) {
        database.insert(list)
    }


}