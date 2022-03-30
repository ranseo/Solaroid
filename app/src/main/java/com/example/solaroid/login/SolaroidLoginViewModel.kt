package com.example.solaroid.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.map


class SolaroidLoginViewModel : ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseAuthLiveData().map { user ->
        if(user != null) {
            val invalid = user.isEmailVerified
            if(invalid) {
                AuthenticationState.AUTHENTICATED
            } else AuthenticationState.INVALID_AUTHENTICATION
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    private val _loginBtn = MutableLiveData<Boolean>(false)
    val loginBtn : LiveData<Boolean>
        get() = _loginBtn

    private val _signUpBtn = MutableLiveData<Boolean>(false)
    val signUpBtn : LiveData<Boolean>
        get() = _signUpBtn


    //////////////////////

    fun onLogin() {
        _loginBtn.value = true
    }

    fun doneLogin() {
        _loginBtn.value = false
    }

    fun onSignUp() {
        _signUpBtn.value = true
    }

    fun doneSignUp() {
        _signUpBtn.value = false
    }




}