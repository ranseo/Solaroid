package com.example.solaroid.login.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidProfileViewModelFactory(val dataSource:DatabasePhotoTicketDao, val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(SolaroidProfileViewModel::class.java)) {
            return SolaroidProfileViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}