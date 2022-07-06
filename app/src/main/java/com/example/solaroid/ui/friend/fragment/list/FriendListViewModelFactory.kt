package com.example.solaroid.ui.friend.fragment.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.room.DatabasePhotoTicketDao

class FriendListViewModelFactory(val dataSource: DatabasePhotoTicketDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(FriendListViewModel::class.java)) {
            return FriendListViewModel(dataSource) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")

    }
}