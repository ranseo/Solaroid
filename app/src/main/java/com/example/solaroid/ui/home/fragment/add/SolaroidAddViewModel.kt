package com.example.solaroid.ui.home.fragment.add

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.*
import com.example.solaroid.Event
import com.example.solaroid.convertTodayToFormatted
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.datasource.photo.PhotoTicketListenerDataSource
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.repositery.phototicket.PhotoTicketRepositery
import com.example.solaroid.models.domain.MediaStoreData
import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.models.room.asFirebaseModel
import com.example.solaroid.repositery.album.AlbumRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

class SolaroidAddViewModel(
    dataSource: DatabasePhotoTicketDao,
    application: Application,
) :
    AndroidViewModel(application) {

    private val database = dataSource

    private val fbAuth = FirebaseManager.getAuthInstance()
    private val fbDatabase = FirebaseManager.getDatabaseInstance()
    private val fbStorage = FirebaseManager.getStorageInstance()

    private val photoTicketRepositery =
        PhotoTicketRepositery(
            database,
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

    var albumName : String = ""
    val albumNameList = Transformations.map(albumRepositery.album) { list ->
        list.map {
            it.name
        }
    }

    //AddChoiceFragment, recyclerView??? ????????? data?????? ?????? ?????????????????? loadImage() ???????????? ?????? ???????????????.
    private val _imagesFromMediaStore = MutableLiveData<List<MediaStoreData>>()
    val imagesFromMediaStore: LiveData<List<MediaStoreData>>
        get() = _imagesFromMediaStore

    //image_spin ?????? ?????? ???, toggle
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

    //addChoice ????????????.
    private val _naviToAddChoice = MutableLiveData<Event<Any?>>()
    val naviToAddChoice: LiveData<Event<Any?>>
        get() = _naviToAddChoice

    //addChoiceFrag??? FragmentContainerView??? Visible ????????? ???, back press????????? ????????? ??? ???????????? ?????? ??????.
    private val _backPressed = MutableLiveData<Boolean>(false)
    val backPressed: LiveData<Boolean>
        get() = _backPressed


    private val _editTextClear = MutableLiveData<String?>()
    val editTextClear: LiveData<String?>
        get() = _editTextClear

    /**
     * we observe "uri" value in SolaroidAddFragment
     * to remove SolaroidAddChoiceFragment
     * */
    private val _image = MutableLiveData<String?>()
    val image: LiveData<String?>
        get() = _image

    val isImageUriSet = Transformations.map(image) {
        !it.isNullOrEmpty()
    }

    private val _uriChoiceFromMediaStore = MutableLiveData<Event<Uri?>>()
    val uriChoiceFromMediaStore: LiveData<Event<Uri?>>
        get() = _uriChoiceFromMediaStore

    private val _date = MutableLiveData<String>(convertTodayToFormatted(System.currentTimeMillis()))
    val date: LiveData<String>
        get() = _date

    fun setDate(date: String) {
        _date.value = date
    }

    private var whichAlbum: DatabaseAlbum? = null

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


    //?????? ??? ??? ?????? ?????? ??????
    fun onImageSpin() {
        val toggle = _imageSpin.value!!
        _imageSpin.value = !toggle
    }

    /**
     * addFragment??? choice_image??? image??? set??? ?????????, ?????? addChoiceFragment??? ???????????? ?????? ?????? ????????? ???????????? ??????. reselect_image??? ???????????? image?????? null ?????????
     */
    fun onReselectImage() {
        setImageNull()
    }


    fun insertPhotoTicket() {
        if (image.value.isNullOrEmpty()) return
        viewModelScope.launch {

            try {
                val album = whichAlbum!!
                val new = PhotoTicket(
                    id = "",
                    url = image.value!!,
                    date = date.value!!,
                    frontText = frontText,
                    backText = backText.value!!,
                    favorite = false,
                    albumInfo = listOf(album.id, album.key, album.name)
                )

                photoTicketRepositery.insertPhotoTickets(
                    whichAlbum!!.asFirebaseModel(),
                    new,
                    getApplication()
                )
                clearAddPhotoTicket()
            } catch (error: IOException) {
                error.printStackTrace()
            } catch (error: NullPointerException) {
                error.printStackTrace()
            }

        }
    }

    private fun clearAddPhotoTicket() {
        _backText.value = ""
        frontText = ""
        _editTextClear.value = null
        setImageNull()
    }


    fun navigateToAddChoice() {
        _naviToAddChoice.value = Event(Unit)
    }


    //backPress ?????? ??????
    /**
     * AddChoiceFragment?????? ???????????? backpress button??? ????????? ???
     * ?????? ?????????????????? ???????????? POP ?????? ??????
     * AddChoiceFragment??? INVISIBLE ?????? AddFragment??? ?????? ???????????? ??????.
     * */
    fun onBackPressedInChoice() {
        _backPressed.value = backPressed.value != true
    }


    /**
     * queryMediaStoreData()???????????? ?????? MediaStore????????? ???????????? ???????????? _imagesFromMediaStore??? ?????? ????????? ??????.
     * postValue??? ???????????? ??????????????? ????????? ?????? ???????????? ?????? ???????????? ??????.
     * */
    fun loadImage() {
        viewModelScope.launch {
            val result = queryMediaStoreData()
            _imagesFromMediaStore.value = result
        }

    }

    /**
     * MediaStoreAPI??? ???????????? ????????? ??? ???????????? ????????????
     * ????????? ???????????? ???????????? ???????????? ??????.
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

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(displayNameColumn)
                    val date =
                        convertTodayToFormatted(TimeUnit.SECONDS.toMillis(cursor.getLong(dateColumn)))

                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    val image = MediaStoreData(id, name, date, contentUri)
                    images += image
                }
            }
        }

        return images
    }

    /**
     * spinner??? ?????? ??????????????? ????????? ???????????? ????????? ???
     * spinner??? ?????? ????????? pos??? ???????????? ?????? album??? ??????????????????
     * ???????????? ?????? DatabaseAlbum????????? whichAlbum ???????????? ??????.
     * */
    fun setWhichAlbum(pos: Int) {
        viewModelScope.launch {
            val album = albums.value?.get(pos) ?: return@launch
            whichAlbum = database.getAlbum(album.id)
        }
    }
}