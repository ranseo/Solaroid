package com.example.solaroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.FirebaseAuthLiveData

class NavigationViewModel : ViewModel() {
    val firebaseAuth = FirebaseManager.getAuthInstance()


    private val _naviToLoginAct = MutableLiveData<Event<Any?>>()
    val naviToLoginAct : LiveData<Event<Any?>>
        get() = _naviToLoginAct

    val userEmail = Transformations.map(FirebaseAuthLiveData()){
        it?.let{ user ->
            user.email
        }
    }



    fun navigateToLoginAct() {
        _naviToLoginAct.value = Event(Unit)
    }


}