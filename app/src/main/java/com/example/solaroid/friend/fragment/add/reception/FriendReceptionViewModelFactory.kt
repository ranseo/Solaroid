package com.example.solaroid.friend.fragment.add.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.data.domain.Profile


class FriendReceptionViewModelFactory(val myProfile: Profile): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(FriendReceptionViewModel::class.java))
            return FriendReceptionViewModel(myProfile) as T
        throw IllegalArgumentException("UNKNOWN_CLASS")
    }

}