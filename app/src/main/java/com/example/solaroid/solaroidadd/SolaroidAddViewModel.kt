package com.example.solaroid.solaroidadd

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.PhotoTicketRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class SolaroidAddViewModel(dataSource: DatabasePhotoTicketDao, application: Application) : AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val photoTicketRepositery = PhotoTicketRepositery(database, fbAuth, fbDatabase, fbStorage)


    private val _photoTicket = MutableLiveData<PhotoTicket?>()
    val photoTicket : LiveData<PhotoTicket?>
        get() = _photoTicket

    //AddChoiceFragment, recyclerView에 보여질 data들을 담는 프로퍼티로써 loadImage() 메서드를 통해 초기화된다.
    private val _imagesFromMediaStore = MutableLiveData<List<MediaStoreData>>()
    val imagesFromMediaStore: LiveData<List<MediaStoreData>>
        get() = _imagesFromMediaStore

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
    private val _naviToAddChoice = MutableLiveData<Event<Any?>>()
    val naviToAddChoice: LiveData<Event<Any?>>
        get() = _naviToAddChoice

    //addChoiceFrag가 FragmentContainerView에 Visible 되었을 때, back press버튼을 눌렀을 때 처리하기 위한 변수.
    private val _backPressed = MutableLiveData<Boolean>(false)
    val backPressed : LiveData<Boolean>
        get() = _backPressed



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

    private val _uriChoiceFromMediaStore = MutableLiveData<Event<Uri?>>()
    val uriChoiceFromMediaStore : LiveData<Event<Uri?>>
        get() = _uriChoiceFromMediaStore

    //navi
    private val _naviToFrameFrag = MutableLiveData<Event<Any?>>()
    val naviToFrameFrag: LiveData<Event<Any?>>
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

    fun setUriChoiceFromMediaStore(uri: Uri) {
        _uriChoiceFromMediaStore.value = Event(uri)
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


    fun insertPhotoTicket() {
        if(image.value.isNullOrEmpty()) return
        viewModelScope.launch {
            val new = PhotoTicket(
                    id = "",
                    url = image.value!!,
                    date = date,
                    frontText = frontText,
                    backText = backText.value!!,
                    favorite = false
                )

            photoTicketRepositery.insertPhotoTickets(new,getApplication())

        }
    }


    fun navigateToAddChoice() {
        _naviToAddChoice.value = Event(Unit)
    }


    //backPress 버튼 처리
    /**
     * AddChoiceFragment에서 사용자가 backpress button을 눌렀을 때
     * 이전 프래그먼트로 돌아가지 POP 되지 않고
     * AddChoiceFragment가 INVISIBLE 되고 AddFragment가 다시 보이도록 설정.
     * */
    fun onBackPressedInChoice() {
        _backPressed.value = backPressed.value != true
    }



    /**
     * queryMediaStoreData()메서드를 통해 MediaStore로부터 이미지를 로드하여 _imagesFromMediaStore의 값을 초기화 한다.
     * postValue를 사용하여 백그라운드 내에서 메인 쓰레드로 값을 지연하여 할당.
     * */
    fun loadImage() {
        viewModelScope.launch {
            val result = queryMediaStoreData()
            _imagesFromMediaStore.postValue(result)
        }

    }

    /**
     * MediaStoreAPI를 이용하여 갤러리 내 사진들을 읽어오고
     * 읽어온 사진들을 리스트에 저장하여 반환.
     * */
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