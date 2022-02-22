package com.example.solaroid.solaroidframe

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

enum class PhotoTicketFilter {
    LATELY,
    FAVORITE
}

class SolaroidFrameViewModel(dataSource: PhotoTicketDao, application: Application) : ViewModel() {

    val database = dataSource


    private val _tmp = MutableLiveData(database.getAllPhotoTicket())
    val tmp : LiveData<LiveData<List<PhotoTicket>>>
        get() = _tmp
    //adapter에 item으로 전달될 LiveData
    private val _photoTickets = MutableLiveData<List<PhotoTicket>>()
    val photoTickets : LiveData<List<PhotoTicket>>
        get() = _photoTickets


    //포토티켓의 favorite을 할당하는 변ㅅ수
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


    init {
        sortByFilter(PhotoTicketFilter.LATELY)
    }


    //popup menu 정렬 클릭 시, 최신순 정렬


    private val _navigateToDetailFrag = MutableLiveData<Long?>()
    val navigateToDetailFrag: LiveData<Long?>
        get() = _navigateToDetailFrag


    fun naviToDetail(photoTicketKey: Long) {
        _navigateToDetailFrag.value = photoTicketKey
    }

    fun doneNaviToDetailFrag() {
        _navigateToDetailFrag.value = null
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
                Log.d("FrameViewModel", "togglePhotoTicket ${photoTicket.value?.id} : ${favorite}")
            }
        }
    }

    fun onFilterPopupMenu() {
        _popUpMenu.value = true
    }

    fun doneFilterPopupMenu() {
        _popUpMenu.value = false
    }

    fun sortByFilter(filter: PhotoTicketFilter) {
        viewModelScope.launch {
            when (filter) {

                PhotoTicketFilter.LATELY -> {
                    _tmp.value = database.getAllPhotoTicket()
                    Log.d("sortByFilter", "LATELY")
                }
                PhotoTicketFilter.FAVORITE -> {
                    _tmp.value =  database.getFavoritePhotoTicket(true)
                    Log.d("sortByFilter", "FAVORITE")
                }
            }
        }
    }


    suspend fun update(photoTicket: PhotoTicket) {
        database.update(photoTicket)
    }

    suspend fun getPhotoTicket(key: Long): PhotoTicket = database.getPhotoTicket(key)


    fun tmpFunction(photoTicketList:List<PhotoTicket>) {
        _photoTickets.value = photoTicketList
    }


}