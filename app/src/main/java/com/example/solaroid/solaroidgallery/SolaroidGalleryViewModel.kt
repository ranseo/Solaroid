package com.example.solaroid.solaroidgallery

import android.app.Application
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.domain.PhotoTicket
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SolaroidGalleryViewModel(dataSource: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {

    private val database = dataSource


    private val _navigateToDetailFrag = MutableLiveData<Event<Long?>>()
    val navigateToDetailFrag: LiveData<Event<Long?>>
        get() = _navigateToDetailFrag

    private val _naviToFrame = MutableLiveData<Event<Any?>>()
    val naviToFrame: LiveData<Event<Any?>>
        get() = _naviToFrame


    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket


    val photoTickets = database.getAllDatabasePhotoTicket().value?.asDomainModel()

    init {
        initGetPhotoTicket()
    }


    private fun initGetPhotoTicket() {
        viewModelScope.launch {
            _photoTicket.value = getLatestPhotoTicket()
        }
    }


    private suspend fun getLatestPhotoTicket(): PhotoTicket? {
        return database.getLatestTicket()?.asDomainModel()
    }


//    fun onClick() {
//        _navigateToCreateFrag.value = true
//    }
//
//    fun doneNaviToCreateFrag() {
//        _navigateToCreateFrag.value = false
//    }

    fun naviToDetail(key: Long) {
        _navigateToDetailFrag.value = Event(key)
    }

    fun navigateToFrame() {
        _naviToFrame.value = Event(Unit)
    }


    fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
    }

}