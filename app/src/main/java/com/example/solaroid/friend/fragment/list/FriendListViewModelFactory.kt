package com.example.solaroid.friend.fragment.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.database.DatabasePhotoTicketDao

class FriendListViewModelFactory(val dataSource: DatabasePhotoTicketDao, val friendCode:Long): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(FriendListViewModel::class.java)) {
            return FriendListViewModel(dataSource, friendCode) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")

    }
}