package com.example.solaroid.friend.fragment.add.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendReceptionViewModelFactory(val friendCode:Long): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(FriendReceptionViewModel::class.java))
            return FriendReceptionViewModel(friendCode) as T
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }

}