package com.example.solaroid.ui.album.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class AlbumViewModelFactory() : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(AlbumViewModel::class.java)) {
            return AlbumViewModel() as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}