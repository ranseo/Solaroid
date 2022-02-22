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


    private val _photoTicketFilter = MutableLiveData<PhotoTicketFilter>()
    val photoTicketFilter: LiveData<PhotoTicketFilter>
        get() = _photoTicketFilter


    //adapter에 item으로 전달될 LiveData
    private val _photoTickets = database.getAllPhotoTicket()
    val photoTickets = Transformations.switchMap(_photoTickets) {
        sortByFilter(it)
    }


    val _favorite = MutableLiveData<Boolean?>()
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


    private fun sortByFilter(photoTickets: List<PhotoTicket>): LiveData<List<PhotoTicket>> {
        Log.d("SortByFilter", "함수에 진입.")
        val liveData: MutableLiveData<List<PhotoTicket>> = MutableLiveData()
        when (photoTicketFilter.value) {
            PhotoTicketFilter.FAVORITE -> {
                liveData.run {
                    value = photoTickets.filter { it.favorite }
                }
                Log.d("SortByFilter", "즐겨찾기에 진입.")
            }
            else -> {
                liveData.run {
                    value = photoTickets
                }
                Log.d("SortByFilter", "else 에 진입.")
            }
        }
        return liveData
    }


    fun setPhotoTicketFilter(filter: PhotoTicketFilter) {
        _photoTicketFilter.value = filter
    }

    suspend fun update(photoTicket: PhotoTicket) {
        database.update(photoTicket)
    }

    suspend fun getPhotoTicket(key: Long): PhotoTicket = database.getPhotoTicket(key)


}