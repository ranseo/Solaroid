package com.example.solaroid.solaroidgallery

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SolaroidGalleryViewModel(dataSource: PhotoTicketDao, val application: Application) :
    ViewModel() {

    private val database = dataSource

//    private val _navigateToCreateFrag = MutableLiveData<Boolean> ()
//    val navigateToCreateFrag : LiveData<Boolean>
//        get() = _navigateToCreateFrag

    private val _navigateToDetailFrag = MutableLiveData<Long?>()
    val navigateToDetailFrag: LiveData<Long?>
        get() = _navigateToDetailFrag

    private val _naviToFrame = MutableLiveData<Boolean>(false)
    val naviToFrame: LiveData<Boolean>
        get() = _naviToFrame


    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket


    val photoTickets = database.getAllPhotoTicket()

    init {
        initGetPhotoTicket()
    }


    private fun initGetPhotoTicket() {
        viewModelScope.launch {
            _photoTicket.value = getLatestPhotoTicket()
        }
    }


    private suspend fun getLatestPhotoTicket(): PhotoTicket? {
        return database.getLatestTicket()
    }


//    fun onClick() {
//        _navigateToCreateFrag.value = true
//    }
//
//    fun doneNaviToCreateFrag() {
//        _navigateToCreateFrag.value = false
//    }

    fun doneToToast() {
        _photoTicket.value = null
    }

    fun naviToDetail(photoTicketKey: Long) {
        _navigateToDetailFrag.value = photoTicketKey
    }

    fun doneNaviToDetailFrag() {
        _navigateToDetailFrag.value = null
    }

    fun navigateToFrame() {
        _naviToFrame.value = true
    }

    fun doneNavigateToFrame() {
        _naviToFrame.value = false
    }

    fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
    }

}