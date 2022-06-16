package com.example.solaroid.home.fragment.gallery

import android.app.Application
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SolaroidGalleryViewModel(dataSource: DatabasePhotoTicketDao, application: Application) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage: FirebaseStorage = FirebaseManager.getStorageInstance()


    val photoTicketRepositery = PhotoTicketRepositery(database, fbAuth, fbDatabase, fbStorage)

    private val _navigateToDetailFrag = MutableLiveData<Event<String>>()
    val navigateToDetailFrag: LiveData<Event<String>>
        get() = _navigateToDetailFrag

    private val _naviToFrame = MutableLiveData<Event<Any?>>()
    val naviToFrame: LiveData<Event<Any?>>
        get() = _naviToFrame


    val photoTickets : LiveData<List<PhotoTicket>>? = photoTicketRepositery.photoTicketsOrderByLately


    fun naviToDetail(key: String) {
        _navigateToDetailFrag.value = Event(key)
    }

    fun navigateToFrame() {
        _naviToFrame.value = Event(Unit)
    }

}