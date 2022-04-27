package com.example.solaroid.solaroiddetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.domain.PhotoTicket
import kotlinx.coroutines.launch

class SolaroidDetailViewModel(photoTicketKey:String ,dataSource: DatabasePhotoTicketDao) : ViewModel() {

    val database = dataSource

    private val _photoTicket = MutableLiveData<PhotoTicket>()
    val photoTicket : LiveData<PhotoTicket>
        get() = _photoTicket

    private val _spinBackSide = MutableLiveData<Boolean>()
    val spinBackSide : LiveData<Boolean>
        get() = _spinBackSide

    init {
        initGetPhotoTicekt(photoTicketKey)
        initSpinBackSide()
        Log.d("디테일", "작동")
    }

    private fun initSpinBackSide() {
        _spinBackSide.value = false
    }


    private fun initGetPhotoTicekt(photoTicketKey: String) {
        viewModelScope.launch {
            _photoTicket.value = getPhotoTicket(photoTicketKey)!!
        }
    }

    suspend fun getPhotoTicket(photoTicketKey: String) :PhotoTicket = database.getDatabasePhotoTicket(photoTicketKey).asDomainModel()


    fun onClickSpin() {
        _spinBackSide.value = spinBackSide.value != true
    }




}