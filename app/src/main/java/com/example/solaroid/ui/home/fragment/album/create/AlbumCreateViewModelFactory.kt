package com.example.solaroid.ui.home.fragment.album.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao

class AlbumCreateViewModelFactory(val dataSource: DatabasePhotoTicketDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(AlbumCreateViewModel::class.java)) {
            return AlbumCreateViewModel(dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}