package com.example.solaroid.friend.fragment.add.dispatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.domain.Profile

class FriendDispatchViewModelFactory(val myProfile:Profile) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(FriendDispatchViewModel::class.java)) {
            return FriendDispatchViewModel(myProfile) as T
        }
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }

}