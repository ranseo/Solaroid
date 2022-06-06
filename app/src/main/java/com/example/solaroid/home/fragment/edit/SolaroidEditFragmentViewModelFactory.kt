package com.example.solaroid.home.fragment.edit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidEditFragmentViewModelFactory(val photoTicketKey:String, val dataSource: DatabasePhotoTicketDao, val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(SolaroidEditFragmentViewModel::class.java)) {
            return SolaroidEditFragmentViewModel(photoTicketKey, dataSource, application) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}