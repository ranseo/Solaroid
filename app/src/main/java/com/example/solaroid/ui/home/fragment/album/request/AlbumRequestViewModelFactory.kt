package com.example.solaroid.ui.home.fragment.album.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao

class AlbumRequestViewModelFactory(val dataSource: DatabasePhotoTicketDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(AlbumRequestViewModel::class.java)) {
            return AlbumRequestViewModel(dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}