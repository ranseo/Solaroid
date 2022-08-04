package com.ranseo.solaroid.ui.home.fragment.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.ranseo.solaroid.ui.album.viewmodel.AlbumViewModel


class AlbumViewModelFactory(val dataSource:DatabasePhotoTicketDao) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(AlbumViewModel::class.java)) {
            return AlbumViewModel(dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}