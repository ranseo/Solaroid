package com.example.solaroid.solaroidadd

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.*
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.PhotoTicketDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class SolaroidAddViewModel(dataSource: PhotoTicketDao, application: Application) : AndroidViewModel(application) {

    val database = dataSource

    //
    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket : LiveData<PhotoTicket?>
        get() = _photoTicket

    //addChoice & add
    private val _images = MutableLiveData<List<MediaStoreData>>()
    val images: LiveData<List<MediaStoreData>>
        get() = _images

    //image_spin 버튼 클릭 시, toggle
    private val _imageSpin = MutableLiveData<Boolean>(false)
    val imageSpin: LiveData<Boolean>
        get() = _imageSpin

    private var frontText = ""
    private val _backText = MutableLiveData<String>("")
    val backText: LiveData<String>
        get() = _backText

    val currBackTextLen = Transformations.map(backText) {
        "${it.length}/100"
    }

    //addChoice & add

    //addChoice 설정하기.
    private val _naviToAddChoice = MutableLiveData<Boolean>(false)
    val naviToAddChoice: LiveData<Boolean>
        get() = _naviToAddChoice

    //addChoiceFrag가 FragmentContainerView에 Visible 되었을 때, back press버튼을 눌렀을 때 처리하기 위한 변수.
    private val _backPressed = MutableLiveData<Boolean>(false)
    val backPressed : LiveData<Boolean>
        get() = _backPressed

    //uri값 설정
    /**
     * we observe "uri" value in SolaroidAddFragment
     * to remove SolaroidAddChoiceFragment
     * */
    private val _image = MutableLiveData<String?>()
    val image : LiveData<String?>
        get() = _image

    val isImageUriSet = Transformations.map(image){
        !it.isNullOrEmpty()
    }

    private val _uri = MutableLiveData<Uri?>()
    val uri : LiveData<Uri?>
        get() = _uri

    //navi
    private val _naviToFrameFrag = MutableLiveData<Boolean>(false)
    val naviToFrameFrag: LiveData<Boolean>
        get() = _naviToFrameFrag



    val date = convertTodayToFormatted(System.currentTimeMillis())


    init {
        loadImage()
    }

    fun onTextChangedFront(s: CharSequence) {
        frontText = s.toString()
    }

    fun onTextChangedBack(s: CharSequence) {
        _backText.value = s.toString()
    }

    /**
     * set _image.value, and showDialog
     */
    fun setImageValue(uri: String) {
        _image.value = uri
    }

    /**
     * set _image.value NULL, and done ShowDialog()
     */
    fun setImageNull() {
        _image.value = null
    }

    fun setUri(uri: Uri) {
        _uri.value = uri
    }

    fun setUriNull() {
        _uri.value = null
    }



    //버튼 및 뷰 클릭 관련 함수
    fun onImageSpin() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }

    /**
     * addFragment의 choice_image의 image가 set된 이후에, 다시 addChoiceFragment로 돌아가기 위해 해당 사진을 취소하는 경우. reselect_image를 클릭하면 image값이 null 로변경
     */
    fun onReselectImage(){
        setImageNull()
    }

    fun onInsertPhotoTicket() {
        if(image.value == null) return
        viewModelScope.launch {
            val newPhotoTicket =
                PhotoTicket(
                    photo = image.value!!,
                    date = date,
                    frontText = frontText,
                    backText = backText.value!!,
                    favorite = false
                )
            val ins = async {
                insert(newPhotoTicket)
            }

            if(ins.await() == Unit) {
                _photoTicket.value = database.getLatestTicket()
            }
        }

    }


    fun navigateToAddChoice() {
        _naviToAddChoice.value = true
    }

    fun doneNavigateToAddChoice() {
        _naviToAddChoice.value = false
    }

    private suspend fun insert(photoTicket: PhotoTicket) {
        database.insert(photoTicket)
    }

    //backPress 버튼 처리
    fun onBackPressedInChoice() {
        val toggle = _backPressed.value!!
        _backPressed.value = !toggle
    }

    //MediaStore API

    fun loadImage() {
        viewModelScope.launch {
            val result = queryMediaStoreData()
            _images.postValue(result)
        }

    }

    private suspend fun queryMediaStoreData(): List<MediaStoreData> {
        val images = mutableListOf<MediaStoreData>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val query = getApplication<Application>().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query?.use{ cursor->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while(cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(displayNameColumn)
                    val date = convertTodayToFormatted(TimeUnit.SECONDS.toMillis(cursor.getLong(dateColumn)))

                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    val image = MediaStoreData(id, name, date,contentUri)
                    images += image
                }
            }
        }

        return images
    }
}