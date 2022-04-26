package com.example.solaroid.solaroidcreate

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.PhotoTicketRepositery
import com.example.solaroid.solaroidframe.SolaroidFrameFragmentContainer
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SolaroidPhotoCreateViewModel(application: Application, dataSource: DatabasePhotoTicketDao) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val photoTicketRepositery = PhotoTicketRepositery(dataSource = database, fbAuth , fbDatabase ,fbStorage)

    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket


    //카메라 촬영 및 캡쳐를 시작하는 프로퍼티
    private val _startImageCapture = MutableLiveData<Event<Any?>>()
    val startImageCapture: LiveData<Event<Any?>>
        get() = _startImageCapture


    //카메라 촬영 이후 이미지 캡처에 성공하면 해당 프로퍼티에 content uri값을 set
    private val _capturedImageUri = MutableLiveData<Uri?>(null)
    val capturedImageUri: LiveData<Uri?>
        get() = _capturedImageUri


    //버튼클릭 시 카메라 셀렉터 전환. (BACK <-> FRONT) , false->BACK, true->FRONT
    private val _cameraConverter = MutableLiveData<Boolean>(false)
    val cameraConverter: LiveData<Boolean>
        get() = _cameraConverter


    //포토티켓 저장 이후 해당 포토티켓과 관련된 back & front text를 모두 clear 하는 프로퍼티.
    private val _editTextClear = MutableLiveData<String?>()
    val editTextClear: LiveData<String?>
        get() = _editTextClear


    //image_spin 버튼 클릭 시, 포토티켓의 반대면을 보여주는 프로퍼티티
   private val _imageSpin = MutableLiveData<Boolean>(false)
    val imageSpin: LiveData<Boolean>
        get() = _imageSpin


    //이미지 캡처 성공 시, view visibility 전환. -> 카메라 촬영 preview 화면에서 이미지 저장 화면으로 전환
    val isLayoutCaptureVisible = Transformations.map(_capturedImageUri) {
        it == null
    }
    val isLayoutCreateVisible = Transformations.map(_capturedImageUri) {
        it != null
    }


    private var frontText: String = ""
    private val _backText = MutableLiveData<String>("")
    val backText: LiveData<String>
        get() = _backText

    val currBackTextLen = Transformations.map(backText) {
        "${it.length}/100"
    }


    val today = convertTodayToFormatted(System.currentTimeMillis())



    private suspend fun insert(photoTicket: PhotoTicket) {
        database.insert(photoTicket)
    }

    private suspend fun getLatestPhotoTicket(): PhotoTicket? {
        return database.getLatestTicket()
    }


    fun onTextChangedFront(s: CharSequence) {
        frontText = s.toString()
    }
    fun onTextChangedBack(s: CharSequence) {
        _backText.value = s.toString()
    }


    fun setCapturedImageUri(savedUri: Uri) {
        _capturedImageUri.value = savedUri
    }

    fun onImageCapture() {
        _startImageCapture.value = Event(Unit)
    }

    //이 부분이 바껴야 할듯 사진촬영 -> room -> firebase 라면
    //사진촬영 -> 촬영된 이미지를 firebase storage 에 저장 -> realtime database에 저장 -> 해당 realtime database로 부터 사진 가져와서 room데이터베이스에 저장
    //이게 repositery를 통해서 이루어져야 한다. 오케이?
    fun onImageSave() {
        viewModelScope.launch {
            val new =
                PhotoTicket(
                    url = capturedImageUri.value.toString(),
                    date = today,
                    frontText = frontText,
                    backText = backText.value!!,
                    favorite = false
                )
            photoTicketRepositery.insertPhotoTickets(new, getApplication())

            forReadyNewImage()
        }
    }


    fun forReadyNewImage() {
        _capturedImageUri.value = null
        _editTextClear.value = null
    }


    fun convertCameraSelector() {
        val toggle = cameraConverter.value!!
        _cameraConverter.value = !toggle
    }

    fun onImageSpin() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }


}