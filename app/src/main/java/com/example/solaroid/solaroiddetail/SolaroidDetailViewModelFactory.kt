package com.example.solaroid.solaroiddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidDetailViewModelFactory(val photoTicketkey: Long, val dataSource: DatabasePhotoTicketDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidDetailViewModel::class.java)) {
            return SolaroidDetailViewModel(photoTicketkey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}