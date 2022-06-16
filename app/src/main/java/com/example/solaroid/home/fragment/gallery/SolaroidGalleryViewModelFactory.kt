package com.example.solaroid.home.fragment.gallery

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidGalleryViewModelFactory(private val dataSource: DatabasePhotoTicketDao, val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidGalleryViewModel::class.java)) {
            return SolaroidGalleryViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}