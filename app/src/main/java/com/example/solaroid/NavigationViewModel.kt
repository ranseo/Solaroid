package com.example.solaroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.solaroid.firebase.FirebaseManager

class NavigationViewModel : ViewModel() {
    val firebaseAuth = FirebaseManager.getAuthInstance()

    private val _naviToLoginAct = MutableLiveData<Event<Any?>>()
    val naviToLoginAct : LiveData<Event<Any?>>
        get() = _naviToLoginAct

    fun navigateToLoginAct() {
        _naviToLoginAct.value = Event(Unit)
    }

    fun getUserEmail() = firebaseAuth.currentUser?.email.toString()


}