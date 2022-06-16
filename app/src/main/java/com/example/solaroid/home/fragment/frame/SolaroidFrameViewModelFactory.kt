package com.example.solaroid.home.fragment.frame

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidFrameViewModelFactory(val dataSource:DatabasePhotoTicketDao, val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidFrameViewModel::class.java)) {
            return SolaroidFrameViewModel(dataSource, application ) as T
        }
        throw IllegalArgumentException("Unknown_class")
    }

}