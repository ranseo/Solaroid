package com.example.solaroid.ui.album.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao


class AlbumViewModelFactory(val dataSource:DatabasePhotoTicketDao) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(AlbumViewModel::class.java)) {
            return AlbumViewModel(dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}