package com.example.solaroid.solaroidframe

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

class SolaroidFrameViewModel(dataSource:PhotoTicketDao, application:Application): ViewModel() {

    val database = dataSource
    val photoTickets = database.getAllPhotoTicket()

    private val _navigateToDetailFrag = MutableLiveData<Long?> ()
    val navigateToDetailFrag : LiveData<Long?>
        get() = _navigateToDetailFrag


    fun naviToDetail(photoTicketKey:Long) {
        _navigateToDetailFrag.value = photoTicketKey
    }

    fun doneNaviToDetailFrag() {
        _navigateToDetailFrag.value = null
    }


}