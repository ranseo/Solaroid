package com.example.solaroid

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.friend.fragment.add.FriendAddViewModel
import com.example.solaroid.room.DatabasePhotoTicketDao

class NavigationViewModelFactory(val dataSource: DatabasePhotoTicketDao, val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(NavigationViewModel::class.java)) {
            return NavigationViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }
}