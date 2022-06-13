package com.example.solaroid.friend.fragment.add.dispatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendDispatchViewModelFactory(val friendCode:Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(FriendDispatchViewModel::class.java)) {
            return FriendDispatchViewModel(friendCode) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }

}