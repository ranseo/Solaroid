package com.example.solaroid.solaroidadd

import androidx.lifecycle.*
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

class SolaroidAddViewModel(dataSource:PhotoTicketDao) : ViewModel() {

    val database = dataSource

    //image_spin 버튼 클릭 시, toggle
    private val _imageSpin = MutableLiveData<Boolean>(false)
    val imageSpin : LiveData<Boolean>
        get() = _imageSpin

    private var frontText = ""
    private val _backText = MutableLiveData<String>("")
    val backText : LiveData<String>
        get() = _backText

    val currBackTextLen = Transformations.map(backText) {
        "${it.length}/100"
    }

    //navi
    private val _naviToFrameFrag = MutableLiveData<Boolean>(false)
    val naviToFrameFrag : LiveData<Boolean>
        get() = _naviToFrameFrag

    val date = convertTodayToFormatted(System.currentTimeMillis())


    init {
        viewModelScope.launch {
        }
    }

    fun onTextChangedFront(s:CharSequence) {
        frontText = s.toString()
    }

    fun onTextChangedBack(s:CharSequence) {
        _backText.value = s.toString()
    }

    //버튼 및 뷰 클릭 관련 함수
    fun onImageSpin() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }

    fun onInsertPhotoTicket() {
        viewModelScope.launch {
            val newPhotoTicket =
                PhotoTicket(photo = "Asd", date = date, frontText = frontText, backText = backText.value!!, favorite = false)
            insert(newPhotoTicket)
        }

    }



    //네비게이션
    fun navigateToFrame() {
        _naviToFrameFrag.value = true
    }

    fun doneNavigateToFrame() {
        _naviToFrameFrag.value = false
    }

    suspend fun insert(photoTicket:PhotoTicket) {
        database.insert(photoTicket)
    }
}