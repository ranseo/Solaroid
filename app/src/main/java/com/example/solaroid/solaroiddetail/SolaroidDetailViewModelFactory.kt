package com.example.solaroid.solaroiddetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.PhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidDetailViewModelFactory(val photoTicketkey: Long, val dataSource: PhotoTicketDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidDetailViewModel::class.java)) {
            return SolaroidDetailViewModel(photoTicketkey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}