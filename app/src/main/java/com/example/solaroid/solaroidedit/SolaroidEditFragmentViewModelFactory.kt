package com.example.solaroid.solaroidedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidEditFragmentViewModelFactory(val photoTicketKey:String, val dataSource: DatabasePhotoTicketDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(SolaroidEditFragmentViewModel::class.java)) {
            return SolaroidEditFragmentViewModel(photoTicketKey, dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}