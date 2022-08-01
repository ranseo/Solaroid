package com.example.solaroid.ui.home.fragment.create

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.models.room.asFirebaseModel
import com.example.solaroid.repositery.album.AlbumRepositery
import com.example.solaroid.repositery.phototicket.PhotoTicketRepositery
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.NullPointerException

class SolaroidPhotoCreateViewModel(
    application: Application,
    dataSource: DatabasePhotoTicketDao,
) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val photoTicketRepositery = PhotoTicketRepositery(
        dataSource = database,
        fbAuth,
        fbDatabase,
        fbStorage,
        PhotoTicketListenerDataSource()
    )
    private val albumRepositery = AlbumRepositery(
        database,
        fbAuth,
        fbDatabase,
        AlbumDataSource()
    )

    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket: LiveData<PhotoTicket?>
        get() = _photoTicket


    private val albums = albumRepositery.album

    var albumName : String? = ""
    val albumNameList = Transformations.map(albumRepositery.album) { list ->
        list.map {
            it.name
        }
    }



    //카메라 촬영 및 캡쳐를 시작하는 프로퍼티
    private val _startImageCapture = MutableLiveData<Event<Any?>>()
    val startImageCapture: LiveData<Event<Any?>>
        get() = _startImageCapture


    //카메라 촬영 이후 이미지 캡처에 성공하면 해당 프로퍼티에 content uri값을 set
    private val _capturedImageUri = MutableLiveData<Uri?>(null)
    val capturedImageUri: LiveData<Uri?>
        get() = _capturedImageUri


    //버튼클릭 시 카메라 셀렉터 전환. (BACK <-> FRONT) , false->BACK, true->FRONT
    private val _cameraConverter = MutableLiveData(false)
    val cameraConverter: LiveData<Boolean>
        get() = _cameraConverter

    //버튼 클릭 시 카메라 셀렉터 변경



    //포토티켓 저장 이후 해당 포토티켓과 관련된 back & front text를 모두 clear 하는 프로퍼티.
    private val _editTextClear = MutableLiveData<String?>()
    val editTextClear: LiveData<String?>
        get() = _editTextClear


    //image_spin 버튼 클릭 시, 포토티켓의 반대면을 보여주는 프로퍼티티
    private val _imageSpin = MutableLiveData(false)
    val imageSpin: LiveData<Boolean>
        get() = _imageSpin

    //progressBar Visible
    private val _isProgressBar = MutableLiveData<Boolean>(false)
    val isProgressBar: LiveData<Boolean>
        get() = _isProgressBar


    //이미지 캡처 성공 시, view visibility 전환. -> 카메라 촬영 preview 화면에서 이미지 저장 화면으로 전환
    val isLayoutCaptureVisible = Transformations.map(_capturedImageUri) {
        it == null
    }
    val isLayoutCreateVisible = Transformations.map(_capturedImageUri) {
        it != null
    }


    private var frontText: String = ""
    private val _backText = MutableLiveData("")
    val backText: LiveData<String>
        get() = _backText

    val currBackTextLen = Transformations.map(backText) {
        "${it.length}/100"
    }

    private var whichAlbum: DatabaseAlbum? = null


    val today = convertTodayToFormatted(System.currentTimeMillis()).substring(0, 13)


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


    init {
    }
    /**
     * 이미지 저장 버튼을 누를 때 url,date,text,favorite 값을 기반으로한 포토티켓 객체를 만들고 이를 photoTicketRepositery.insert()의 매개변수로 전달하여
     * room 과 firebase 내 database에 삽입하는 함수.
     * */
    fun onImageSave() {
        viewModelScope.launch {

            try {

                val album =whichAlbum!!
                val new =
                    PhotoTicket(
                        id = "",
                        url = capturedImageUri.value.toString(),
                        date = convertTodayToFormatted(System.currentTimeMillis()),
                        frontText = frontText,
                        backText = backText.value!!,
                        favorite = false,
                        albumInfo = listOf(album.id, album.key, album.name)
                    )

                Log.i(TAG, "onImageSave()")

                _isProgressBar.value = true
                photoTicketRepositery.insertPhotoTickets(
                    album.asFirebaseModel(),
                    new,
                    getApplication()
                )
                _isProgressBar.value = false
                forReadyNewImage()

            } catch (error:IOException) {
                error.printStackTrace()
            } catch (error:NullPointerException) {
                error.printStackTrace()
            }
        }
    }


    /**
     * 이미지 저장이 완료되면 다시 카메라 촬영 즉 preview 화면으로 전환하고 기존의 포토티켓을 초기화 하는 함수
     * */
    private fun forReadyNewImage() {
        _capturedImageUri.value = null
        _editTextClear.value = null
    }


    /**
     * preview화면에서 전면 및 후면 카메라를 선택하는 함수.
     * */
    fun convertCameraSelector() {
        _cameraConverter.value = cameraConverter.value != true
    }

    /**
     * 포토티켓의 side를 바꿀 수 있는 함수.
     * */
    fun onImageSpin() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }

    /**
     * spinner를 통해 포토티켓이 소유될 사진첩을 골랐을 때
     * spinner의 선택 목록의 pos를 이용하여 어떤 album이 선택되었는지
     * 확인하고 해당 DatabaseAlbum객체를 whichAlbum 할당하는 함수.
     * */
    fun setWhichAlbum(pos: Int) {
        viewModelScope.launch {
            val album = albums.value?.get(pos) ?: return@launch
            whichAlbum = database.getAlbum(album.id)
        }
    }


    companion object {
        const val TAG = "크리에이트뷰모델"
    }

}