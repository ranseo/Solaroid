package com.example.solaroid.home.fragment.frame

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.home.fragment.gallery.PhotoTicketFilter
import com.example.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidFrameViewModelFactory(val dataSource:DatabasePhotoTicketDao, val application: Application, val filter:PhotoTicketFilter, val photoTicket:PhotoTicket) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("Unchecked_cast")
        if(modelClass.isAssignableFrom(SolaroidFrameViewModel::class.java)) {
            return SolaroidFrameViewModel(dataSource, application, filter, photoTicket ) as T
        }
        throw IllegalArgumentException("Unknown_class")
    }

}