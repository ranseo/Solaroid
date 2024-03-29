package com.ranseo.solaroid.ui.home.fragment.create

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidPhotoCreateViewModelFactory(private val dataSource: DatabasePhotoTicketDao, val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidPhotoCreateViewModel::class.java)) {
            return SolaroidPhotoCreateViewModel(application, dataSource) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}