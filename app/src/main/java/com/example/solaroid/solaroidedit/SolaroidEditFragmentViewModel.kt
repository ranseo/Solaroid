package com.example.solaroid.solaroidedit

import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolaroidEditFragmentViewModel(photoTicketKey:Long, dataSource:PhotoTicketDao) : ViewModel() {

    val database = dataSource
    private val _photoTicket = MutableLiveData<PhotoTicket>()
    val photoTicket : LiveData<PhotoTicket>
        get() = _photoTicket

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


    init {
        viewModelScope.launch {
            _photoTicket.value = getPhotoTicket(photoTicketKey)!!
            Log.d("에디트", "photoTicket key : ${photoTicket.value?.id}")
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


    fun onUpdatePhotoTicket() {
        viewModelScope.launch {
            val curr = _photoTicket.value!!
            val new = PhotoTicket(curr.id, curr.photo , frontText, _backText.value!!, curr.date, curr.favorite)
            update(new)
            _photoTicket.value = new

        }
    }

    override fun onCleared() {
        super.onCleared()
    }



    //데이터베이스 관련 함수
    suspend fun getPhotoTicket(key:Long) : PhotoTicket {
        return database.getPhotoTicket(key)
    }

    suspend fun update(photoTicket:PhotoTicket) = withContext(Dispatchers.IO) { database.update(photoTicket)}
}