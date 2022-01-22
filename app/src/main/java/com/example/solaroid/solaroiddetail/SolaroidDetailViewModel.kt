package com.example.solaroid.solaroiddetail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import com.example.solaroid.database.SolaroidDatabase
import kotlinx.coroutines.launch

class SolaroidDetailViewModel(photoTicketKey:Long ,dataSource: PhotoTicketDao) : ViewModel() {

    val database = dataSource

    private val _photoTicket = MutableLiveData<PhotoTicket>()
    val photoTicket : LiveData<PhotoTicket>
        get() = _photoTicket

    init {
        initGetPhotoTicekt(photoTicketKey)
    }

    private fun initGetPhotoTicekt(photoTicketKey: Long) {
        viewModelScope.launch {
            _photoTicket.value = getPhotoTicket(photoTicketKey)!!
        }
    }

    suspend fun getPhotoTicket(photoTicketKey: Long) :PhotoTicket = database.getPhotoTicket(photoTicketKey)






}