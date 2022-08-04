package com.ranseo.solaroid.ui.login.viewmodel

import androidx.lifecycle.*
import com.ranseo.solaroid.Event

class SolaroidSignUpViewModel : ViewModel() {

    enum class SignUpErrorType {
        EMAILTYPEERROR, PASSWORDERROR, ISRIGHT, EMPTY
    }

    private val _signUpBtn = MutableLiveData<Event<Any?>>()
    val signUpBtn : LiveData<Event<Any?>>
        get() = _signUpBtn


    private val _signUpErrorType = MutableLiveData<SignUpErrorType>()
    val signUpErrorType : LiveData<SignUpErrorType>
        get() = _signUpErrorType

    val isSingUpAlert = Transformations.map(signUpErrorType){ type ->
        when(type) {
            SignUpErrorType.ISRIGHT, SignUpErrorType.EMPTY -> false
            else -> true
        }
    }

    val alertText = Transformations.map(signUpErrorType){ type->
        val prefix = "회원가입 실패 : "
        prefix + when(type) {
            SignUpErrorType.EMAILTYPEERROR -> "올바른 이메일 주소 형식을 입력하세요."
            SignUpErrorType.PASSWORDERROR -> "올바른 비밀번호를 입력하세요."
            else -> ""
        }
    }

    //////////////////////

    private val _naviToLogin = MutableLiveData<Event<Any?>>()
    val naviToLogin : LiveData<Event<Any?>>
        get() = _naviToLogin


    fun navigateToLogin() {
        _naviToLogin.value = Event(Unit)
    }

    //////////////////////


    fun onSignUp() {
        _signUpBtn.value = Event(Unit)
    }

    fun setSignUpErrorType(type : SignUpErrorType) {
        _signUpErrorType.value = type
    }


}