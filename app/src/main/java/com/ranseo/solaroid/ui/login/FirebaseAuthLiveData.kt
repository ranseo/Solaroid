package com.ranseo.solaroid.ui.login

import androidx.lifecycle.LiveData
import com.ranseo.solaroid.firebase.FirebaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthLiveData : LiveData<FirebaseUser?>() {
    private val firebaseAuth = FirebaseManager.getAuthInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}