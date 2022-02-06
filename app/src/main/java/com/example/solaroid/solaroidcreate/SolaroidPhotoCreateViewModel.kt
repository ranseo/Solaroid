package com.example.solaroid.solaroidcreate

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

class SolaroidPhotoCreateViewModel(application: Application, dataSource: PhotoTicketDao) :
    ViewModel() {

    val database = dataSource

    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket

    private var frontText: String = ""
    private var backText: String = ""


    val today = convertTodayToFormatted(System.currentTimeMillis(), application.resources)

    private suspend fun insert(photoTicket: PhotoTicket) {
        database.insert(photoTicket)
    }

    private suspend fun getLatestPhotoTicket(): PhotoTicket? {
        return database.getLatestTicket()
    }

//    fun setFrontText(text: String) {
//        frontText = text
//    }
//
//    fun setBackText(text: String) {
//        backText = text
//    }

    fun onTextChangedFront(s:CharSequence) {
        frontText= s.toString()
    }

    fun onTextChangedBack(s:CharSequence) {
        backText= s.toString()
    }

    fun onClick() {
        viewModelScope.launch {
            val newPhotoTicket =
                PhotoTicket(photo = true, date = today, frontText = frontText, backText = backText)
            insert(newPhotoTicket)
            _photoTicket.value = getLatestPhotoTicket()
        }
    }

    fun doneNavigateToGalleryFragment() {
        _photoTicket.value = null
    }


}