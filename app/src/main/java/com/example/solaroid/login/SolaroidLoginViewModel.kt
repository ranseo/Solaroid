package com.example.solaroid.login

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.example.solaroid.Event


class SolaroidLoginViewModel : ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    enum class LoginErrorType {
        EMAILTYPEERROR, PASSWORDERROR, ACCOUNTERROR,ISRIGHT, INVALID,  EMPTY
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

    private val _loginBtn = MutableLiveData<Event<Any?>>()
    val loginBtn : LiveData<Event<Any?>>
        get() = _loginBtn


    private val _loginErrorType = MutableLiveData<LoginErrorType>()
    val loginErrorType : LiveData<LoginErrorType>
        get() = _loginErrorType

    val isSingUpAlert = Transformations.map(loginErrorType){ type ->
        when(type) {
            LoginErrorType.ISRIGHT, LoginErrorType.EMPTY -> false
            else -> true
        }
    }


    val alertText = Transformations.map(loginErrorType){ type->
        val prefix = "※"
        prefix + when(type) {
            LoginErrorType.EMAILTYPEERROR -> "올바른 이메일 주소 형식을 입력하세요."
            LoginErrorType.PASSWORDERROR -> "올바른 비밀번호를 입력하세요."
            LoginErrorType.ACCOUNTERROR -> "이메일 혹은 비밀번호가 틀렸습니다."
            LoginErrorType.INVALID -> "본인 인증이 되지 않았습니다."
            else -> ""
        }
    }

    //////////////////////

    private val _naviToSignUp = MutableLiveData<Event<Any?>>()
    val naviToSignUp : LiveData<Event<Any?>>
     get() = _naviToSignUp


    fun navigateToSignUp() {
        _naviToSignUp.value = Event(Unit)
    }

    //////////////////////

    fun onLogin() {
        _loginBtn.value = Event(Unit)
    }


    fun setLoginErrorType(type : LoginErrorType) {
        _loginErrorType.value = type
    }




}