package com.example.solaroid.login.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.solaroid.Event

class SolaroidProfileViewModel : ViewModel() {

    enum class ProfileErrorType{
        IMAGEERROR, NICKNAMEERROR, ISRIGHT, EMPTY
    }

    private val _nickname = MutableLiveData<String>("")
    val nickname : LiveData<String>
        get() = _nickname


    val nicknameLen = Transformations.map(nickname) {
        val len = if(it.isNullOrEmpty()) 0
        else it.length

        "${len}/12"
    }

    private val _profileUrl = MutableLiveData<String?>()
    val profileUrl : LiveData<String?>
        get() = _profileUrl

    val isSetProfile = Transformations.map(profileUrl) {
        it!=null
    }

    private val _profileType = MutableLiveData<ProfileErrorType>()
    val profileType : LiveData<ProfileErrorType>
        get() = _profileType

    fun setProfileType(type: ProfileErrorType) {
        _profileType.value = type
    }

    val isAlamVisible = Transformations.map(profileType) { type ->
        when(type) {
            ProfileErrorType.ISRIGHT, ProfileErrorType.EMPTY -> false
            else -> true
        }
    }

    val alertMessage = Transformations.map(profileType) { type ->
        "※"+when(type) {
            ProfileErrorType.NICKNAMEERROR -> "별명을 입력해주세요"
            ProfileErrorType.IMAGEERROR-> "프로필 사진을 설정해주세요"
            else -> ""
        }
    }



    fun onNicknameEditTextChanged(str: CharSequence) {
        _nickname.value = str.toString()
    }

    fun setProfileUrl(uri: Uri) {
        _profileUrl.value = uri.toString()
    }

    ///////////////

    private val _addImage = MutableLiveData<Event<Any?>>()
    val addImage : LiveData<Event<Any?>>
        get() = _addImage


    fun onAddBtn() {
        _addImage.value = Event(Unit)
    }

    private val _setProfile = MutableLiveData<Event<Any?>>()
    val setProfile : LiveData<Event<Any?>>
        get() = _setProfile

    fun onSetProfile() {
        _setProfile.value = Event(Unit)
    }

    ///////////////




}