package com.example.solaroid.solaroidgallery

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.PhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidGalleryViewModelFactory(private val dataSource: PhotoTicketDao, val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidGalleryViewModel::class.java)) {
            return SolaroidGalleryViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}