package com.example.solaroid.solaroidcreate

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.launch

class SolaroidPhotoCreateViewModel(application: Application, dataSource: PhotoTicketDao) :
    ViewModel() {

    val database = dataSource

    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket

    private val _startImageCapture = MutableLiveData<Boolean>()
    val startImageCapture : LiveData<Boolean>
        get() = _startImageCapture


    //Image를 capture에 성공하면 해당 프로퍼티에 uri를 설정.
    private val _capturedImageUri = MutableLiveData<Uri?>(null)
    val capturedImageUri : LiveData<Uri?>
        get() = _capturedImageUri


    //버튼클릭 시 카메라 셀렉터 전환. (BACK <-> FRONT) , false->BACK, true->FRONT
    private val _cameraConverter = MutableLiveData<Boolean>(false)
    val cameraConverter : LiveData<Boolean>
        get() = _cameraConverter

    private val _editTextClear = MutableLiveData<String?>()
    val editTextClear : LiveData<String?>
        get() = _editTextClear


    //이미지 캡처 성공 시, view visibility 전환.

    val isLayoutCaptureVisible = Transformations.map(_capturedImageUri) {
        it == null
    }

    val isLayoutCreateVisible = Transformations.map(_capturedImageUri) {
        it != null
    }

    private var frontText: String = ""
    private var backText: String = ""


    val today = convertTodayToFormatted(System.currentTimeMillis(), application.resources)


    private suspend fun insert(photoTicket: PhotoTicket) {
        database.insert(photoTicket)
    }

    private suspend fun getLatestPhotoTicket(): PhotoTicket? {
        return database.getLatestTicket()
    }

//    fun setFrontText(text: String) {
//        frontText = text
//    }
//
//    fun setBackText(text: String) {
//        backText = text
//    }

    fun onTextChangedFront(s:CharSequence) {
        frontText= s.toString()
    }

    fun onTextChangedBack(s:CharSequence) {
        backText= s.toString()
    }

/*    fun onClick() {
        viewModelScope.launch {
            val newPhotoTicket =
                PhotoTicket(photo = true, date = today, frontText = frontText, backText = backText)
            insert(newPhotoTicket)
            _photoTicket.value = getLatestPhotoTicket()
        }
    }*/

    fun setCapturedImageUri(savedUri : Uri) {
        _capturedImageUri.value = savedUri
    }


    fun onImageCapture() {
        _startImageCapture.value = true
    }

    fun stopImageCapture() {
        _startImageCapture.value = false
    }

    fun onImageSave() {
        viewModelScope.launch {
            val newPhotoTicket =
                PhotoTicket(photo = capturedImageUri.value.toString(), date = today, frontText = frontText, backText = backText, favorite = false)
            insert(newPhotoTicket)
            _photoTicket.value = getLatestPhotoTicket()

            forReadyNewImage()
        }
    }

    fun forReadyNewImage() {
        _capturedImageUri.value = null
        _editTextClear.value = null
    }

    fun doneNavigateToGalleryFragment() {
        _photoTicket.value = null
    }

    fun convertCameraSelector() {
        val toggle = cameraConverter.value!!
        _cameraConverter.value = !toggle
    }



}