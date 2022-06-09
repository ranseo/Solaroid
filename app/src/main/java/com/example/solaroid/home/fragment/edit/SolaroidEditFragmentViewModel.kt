package com.example.solaroid.home.fragment.edit

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.PhotoTicketRepositery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class SolaroidEditFragmentViewModel(photoTicketKey:String, dataSource: DatabasePhotoTicketDao,application: Application) : AndroidViewModel(application) {

    val database = dataSource


    private val fbAuth: FirebaseAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase: FirebaseDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage : FirebaseStorage = FirebaseManager.getStorageInstance()

    private val photoTicketRepositery : PhotoTicketRepositery = PhotoTicketRepositery(dataSource,fbAuth,fbDatabase, fbStorage)

    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket : LiveData<PhotoTicket?>
        get() = _photoTicket

    //image_spin 버튼 클릭 시, toggle
    private val _imageSpin = MutableLiveData<Boolean>(false)
    val imageSpin : LiveData<Boolean>
        get() = _imageSpin

    private var frontText = ""
    private val _backText = MutableLiveData<String>("")
    val backText : LiveData<String>
        get() = _backText

    private val _date = MutableLiveData<String>()
    val date : LiveData<String>
        get() = _date

    fun setDate(date:String) {
        _date.value = date
    }


    val currBackTextLen = Transformations.map(backText) {
        "${it.length}/300"
    }

    //navi
    private val _naviToFrameFrag = MutableLiveData<Event<Boolean>>()
    val naviToFrameFrag : LiveData<Event<Boolean>>
        get() = _naviToFrameFrag


    init {
        viewModelScope.launch {
            _photoTicket.value = getPhotoTicket(photoTicketKey)
            if(photoTicket.value == null) navigateToFrame()
            frontText = photoTicket.value!!.frontText
            _backText.value = photoTicket.value!!.backText
            setDate(photoTicket.value!!.date)
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
        _imageSpin.value = imageSpin.value != true
    }


    fun onUpdatePhotoTicket() {
        viewModelScope.launch {
            val curr = _photoTicket.value!!
            Log.i(TAG,"${frontText}, ${_backText.value!!}")
            val new = PhotoTicket(curr.id, curr.url , frontText, _backText.value!!, date.value!!, curr.favorite)
            photoTicketRepositery.updatePhotoTickets(new,getApplication())
        }
    }


    suspend fun getPhotoTicket(key:String) : PhotoTicket? {
        return database.getDatabasePhotoTicket(key).asDomainModel()
    }

    //네비게이션
    fun navigateToFrame() {
        _naviToFrameFrag.value = Event(true)
    }

    companion object{
        const val TAG = "에디트프래그먼트"
    }

}