package com.example.solaroid.ui.home.fragment.gallery

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class GalleryViewModelFactory(private val dataSource: DatabasePhotoTicketDao, val application: Application,
                              val albumId:String, val albumKey:String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(dataSource, application, albumId, albumKey) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}