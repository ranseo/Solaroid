package com.example.solaroid.ui.home.fragment.add

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidAddViewModelFactory(val dataSource: DatabasePhotoTicketDao, val application: Application, val albumId:String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(SolaroidAddViewModel::class.java)) {
            return SolaroidAddViewModel(dataSource, application, albumId) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}