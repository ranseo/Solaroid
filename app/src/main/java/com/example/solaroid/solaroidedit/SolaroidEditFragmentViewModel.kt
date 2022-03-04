package com.example.solaroid.solaroidedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

class SolaroidEditFragmentViewModel(photoTicketKey:Long, dataSource:PhotoTicketDao) : ViewModel() {

    val database = dataSource
    private val _photoTicket = MutableLiveData<PhotoTicket>()
    val photoTicket : LiveData<PhotoTicket>
        get() = _photoTicket

    //image_spin 버튼 클릭 시, toggle
    private val _imageSpin = MutableLiveData<Boolean>(false)
    val imageSpin : LiveData<Boolean>
        get() = _imageSpin

    private var frontText = ""
    private val _backText = MutableLiveData<String>("")
    val backText : LiveData<String>
        get() = _backText


    init {
        viewModelScope.launch {
            _photoTicket.value = getPhotoTicket(photoTicketKey)!!
        }
    }

    fun onAfterChangedFront(s:CharSequence) {
        frontText = s.toString()
    }

    fun onAfterChangedBack(s:CharSequence) {
        _backText.value = s.toString()
    }

    //버튼 및 뷰 클릭 관련 함수
    fun onImageSpin() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }


    fun updatePhotoTicket() {
        viewModelScope.launch {

        }
    }


    //데이터베이스 관련 함수
    suspend fun getPhotoTicket(key:Long) : PhotoTicket {
        return database.getPhotoTicket(key)
    }

    suspend fun update(photoTicket:PhotoTicket) {
        database.update(photoTicket)
    }
}