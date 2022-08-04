package com.ranseo.solaroid.ui.home.fragment.edit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import java.lang.IllegalArgumentException

class SolaroidEditFragmentViewModelFactory(val photoTicketKey:String, val dataSource: DatabasePhotoTicketDao, val application: Application, val albumId:String, val albumKey:String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(SolaroidEditFragmentViewModel::class.java)) {
            return SolaroidEditFragmentViewModel(photoTicketKey, dataSource, application, albumId, albumKey) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}