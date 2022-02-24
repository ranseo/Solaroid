package com.example.solaroid.solaroidframe

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

enum class PhotoTicketFilter {
    LATELY,
    FAVORITE
}

class SolaroidFrameViewModel(dataSource: PhotoTicketDao, application: Application) : ViewModel() {

    val database = dataSource



    private var photoTicketFilter = PhotoTicketFilter.LATELY


    //adapter에 item으로 전달될 LiveData
    var photoTickets = database.getAllPhotoTicket()

    private val _favorite = MutableLiveData<Boolean?>()
    val favorite: LiveData<Boolean?>
        get() = _favorite

    //viewPager2의 각 page 위치에 배치된 item (=PhotoTicket)을 할당.
    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket

    //버튼 클릭 시, popup Menu
    private val _popUpMenu = MutableLiveData<Boolean>(false)
    val popUpMenu: LiveData<Boolean>
        get() = _popUpMenu



    private val _navigateToDetailFrag = MutableLiveData<Long?>()
    val navigateToDetailFrag: LiveData<Long?>
        get() = _navigateToDetailFrag

    //즐겨찾기 또는 최신순 정렬을 위한 Toggle 설정. -> 해당 toggle이 변화하면 submit
    private val _navigateToFrameFrag = MutableLiveData<Boolean>(false)
    val navigateToFrameFrag : LiveData<Boolean>
        get() = _navigateToFrameFrag

    //즐겨찾기 해재 시, 해당 viewPager의 position을 기록 -> 이는 viewPager의 onPageSelected의 문제점을 해결하기 위한 변수
    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition : LiveData<Int>
        get() = _currentPosition


    init {
        Log.d("FrameViewModel","Init")
    }

    private fun initPhotoTickets(filter:PhotoTicketFilter) {
        sortByFilter(filter)
    }




    fun setCurrentPhotoTicket(curr: PhotoTicket) {
        Log.d("FrameViewModel", "setCurrentPhotoTicket ${curr.id}")
        _photoTicket.value = curr
        Log.d("FrameViewModel", "setCurrentPhotoTicket ${photoTicket.value?.id}")
    }


    fun setCurrentFavorite(favorite: Boolean) {
        _favorite.value = favorite
    }


    //favorite(즐겨찾기) 값에 따라서 photoTicket를 업데이트하고, 현재 photoTicket값 갱신
    fun togglePhotoTicketFavorite(favorite: Boolean) {
        viewModelScope.launch {
            _photoTicket.value?.let {
                it.favorite = favorite
                update(it)
                _photoTicket.value = getPhotoTicket(it.id)
                Log.d("FrameFragment", "togglePhotoTicket ${photoTicket.value?.id} : ${favorite}")
            }
        }
    }

    fun offPhotoTicketFavorite(favorite: Boolean) {
        viewModelScope.launch {
            _photoTicket.value?.let {
                it.favorite = favorite
                update(it)
                Log.d("FavoriteFrame", "togglePhotoTicket ${photoTicket.value?.id} : ${favorite}")
            }
        }
    }


     fun sortByFilter(filter:PhotoTicketFilter){
            photoTickets = when(filter) {
                PhotoTicketFilter.LATELY -> {
                    Log.d("sortByFilter", "LATELY SUCCESS")
                    database.getAllPhotoTicket()
                }
                PhotoTicketFilter.FAVORITE -> {
                    Log.d("sortByFilter", "FAVORITE SUCCESS")
                    database.getFavoritePhotoTicket(true)
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

    fun naviToDetail(photoTicketKey: Long) {
        _navigateToDetailFrag.value = photoTicketKey
    }

    fun doneNaviToDetailFrag() {
        _navigateToDetailFrag.value = null
    }

    fun naviToFrame(navi: Boolean) {
        _navigateToFrameFrag.value = navi
    }

    fun doneNaviToFrameFrag() {
        _navigateToFrameFrag.value = false
    }

    /////////////////////////////////////////////////////////////////



    fun setCurrentPositionAfterFavoriteOff(position:Int) {
            _currentPosition.value = position


    }

    suspend fun update(photoTicket: PhotoTicket) {
        database.update(photoTicket)
    }

    suspend fun getPhotoTicket(key: Long): PhotoTicket = database.getPhotoTicket(key)


}