package com.example.solaroid.solaroidframe

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.database.DatabasePhotoTicket
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.repositery.PhotoTicketRepositery
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

enum class PhotoTicketFilter {
    LATELY,
    FAVORITE
}

class SolaroidFrameViewModel(dataSource: DatabasePhotoTicketDao, application: Application, val fbUser: FirebaseUser, val fbDatabase: FirebaseDatabase) : AndroidViewModel(application) {



    companion object {
        const val TAG = "프레임뷰모델"
    }

    val database = dataSource

    val photoTicketRepositery :PhotoTicketRepositery = PhotoTicketRepositery(dataSource)
    val photoTickets : LiveData<List<PhotoTicket>> = photoTicketRepositery.photoTickets




    private var photoTicketFilter = PhotoTicketFilter.LATELY

    private val _photoTicketsByFirebase = MutableLiveData<List<DatabasePhotoTicket>>()
    val photoTicketsByFirebase : LiveData<List<DatabasePhotoTicket>>
        get() = _photoTicketsByFirebase


    /**
     * Room으로 부터 얻은 포토티켓 리스트의 사이즈를 갖는 프로퍼티.
     * */
    var photoTicketsSize = Transformations.map(photoTickets) {
        it?.let {
            it.size
        }
    }

    /**
     * 현재 포토티켓의 정렬 방식(최신순 또는 즐겨찾기)에 따라서 UI에 TEXT 표시
     * */
    private val _currFilterText = MutableLiveData<String>("최신순")
    val currFilterText : LiveData<String>
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
     * 현재 viewPager에서 사용자가 보고 있는 페이지에 속한 포토티켓
     * */
    val currPhotoTicket : LiveData<PhotoTicket?>

    /**
     * 오른쪽 상단의 오버플로우 메뉴 클릭 시, popup Menu open/close 하는 프로퍼티
     * */
    private val _popUpMenu = MutableLiveData<Boolean>(false)
    val popUpMenu: LiveData<Boolean>
        get() = _popUpMenu

    //spin_image
    /**
     * 현재 보고있는 포토티켓을 클릭하면 포토티켓이 회전할 수 있도록 만드는 프로퍼티
     * */
    private val _imageSpin = MutableLiveData(false)
    val imageSpin: LiveData<Boolean>
        get() = _imageSpin



    //즐겨찾기 해재 시, 해당 viewPager의 position을 기록 -> 이는 viewPager의 onPageSelected의 문제점을 해결하기 위한 변수

    /**
     * 현재 viewPager에서 사용자가 보고 있는 포토티켓의 위치를 나타내는 프로퍼티.
     * 해당 변수의 값을 이용하여 현재 사용자가 보고 있는 포토티켓의 값을 설정할 수 있다.
     * */
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int>
        get() = _currentPosition

    /**
     * currentPosition의 값 설정.
     * */
    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    init {
        Log.d(TAG, "Init")

        refreshDataFromRepositery(fbUser, fbDatabase)

        // 현재 사용자가 보고 있는 포토티켓을 설정하기 위해서 currentPosition 값을
        // photoTickets의 인덱스로 이용하여 photoTicket을 가져온다.
        // 만약 현재 포지션이 0보다 작다면 아무런 포토티켓이 없는 상태이므로 null값을 대입.
        // 그렇지 않은 경우, 만약 photoTickets이 비어있다면 null 값을 대입.
        // 비어있지 않은 경우 현재 포지션을 idx로 사용하여 PhotoTicket 타입의 원소를 대입한다.
        currPhotoTicket= Transformations.map(currentPosition) { position ->
            if(position>=0) {
                val list = photoTickets.value
                if(list!=null) {
                    list[position]
                } else null
            } else null
        }


    }

    /**
     * PhotoTicketRepositery를 이용하여 firebase realtime database로 부터 Room Database에 넣을 photoTicket Model 을 get.
     * */
    private fun refreshDataFromRepositery(user: FirebaseUser, fbDatabase:FirebaseDatabase) {
        viewModelScope.launch {
            photoTicketRepositery.refreshPhotoTickets(user, fbDatabase)
        }
    }



    //favorite(즐겨찾기) 값에 따라서 DatabasePhotoTicket를 업데이트하고, 현재 DatabasePhotoTicket값 갱신
    /**
     * FrameContainer 프래그먼트에서 포토티켓의 정렬을 즐겨찾기 또는 최신순으로 바꿀 경우
     * */

    /**
     * 포토티켓 즐겨찾기를 등록하거나 해제할 경우, 새로운 즐겨찾기 값으로 해당 포토티켓을 update한다.
     * */
    fun togglePhotoTicketFavorite(favorite: Boolean) {
        viewModelScope.launch {
            currPhotoTicket.value?.let {
                it.favorite = favorite
                //이거 it asDatabaseModel이 있어야할듯.
                //그리고 databaseModel에서도 asFirebaseDataModel이 있으면 좋을듯.
                photoTicketRepositery.updatePhotoTickets(fbUser, fbDatabase, it)
            }
        }
    }

//    fun offDatabasePhotoTicketFavorite(favorite: Boolean) {
//        viewModelScope.launch {
//            _DatabasePhotoTicket.value?.let {
//                it.favorite = favorite
//                update(it)
//                Log.d("프레임프래그먼트", "toggleDatabasePhotoTicket ${DatabasePhotoTicket.value?.id} : ${favorite}")
//            }
//        }
//    }


    /**
     * 수정예정.
     * 프레임컨테이너 프래그먼트 내 fragmentContainer 내에 (즐겨찾기 <-> 최신순) 프래그먼트를 전환할 수 있다.
     * */
    fun sortByFilter(filter: PhotoTicketFilter) {
        DatabasePhotoTickets = when (filter) {
            PhotoTicketFilter.LATELY -> {
                _currFilterText.value = "최신순"
                Log.d("sortByFilter", "LATELY SUCCESS")
                database.getAllDatabasePhotoTicket()
            }
            PhotoTicketFilter.FAVORITE -> {
                _currFilterText.value = "즐겨찾기"
                Log.d("sortByFilter", "FAVORITE SUCCESS")
                database.getFavoriteDatabasePhotoTicket(true)
            }
        }
        DatabasePhotoTicketsSize = Transformations.map(DatabasePhotoTickets) {
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
    fun setDatabasePhotoTicketFilter(filter: PhotoTicketFilter) {
        DatabasePhotoTicketFilter = filter
        Log.d("tag", "${DatabasePhotoTicketFilter}")
    }


//    네비게이션 변수 및 함수.
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


    fun navigateToDetail(DatabasePhotoTicketKey: Long) {
        _naviToDetailFrag.value = DatabasePhotoTicketKey
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

    fun navigateToEdit(DatabasePhotoTicketKey: Long?) {
        DatabasePhotoTicketKey?.let {
            _naviToEditFrag.value = DatabasePhotoTicketKey
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






    suspend fun getDatabasePhotoTicket(key: Long): DatabasePhotoTicket = database.getDatabasePhotoTicket(key)

    //이미지 회전
    fun spinImage() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }


    //onClick

    fun onListItemClick() {
        spinImage()
    }


    fun deleteDatabasePhotoTicket(key: Long) {
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

    private suspend fun update(DatabasePhotoTicket: DatabasePhotoTicket) {
        database.update(DatabasePhotoTicket)
    }

    private suspend fun delete(key: Long) {
        database.delete(key)
    }

    private suspend fun insertAll(list: List<DatabasePhotoTicket>) {
        database.insert(list)
    }


}