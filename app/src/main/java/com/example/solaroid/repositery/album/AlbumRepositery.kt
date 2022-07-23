package com.example.solaroid.repositery.album

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.models.room.asDomainModel
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.models.room.modifyOverrideAlbumName
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.NullPointerException

class AlbumRepositery(
    private val roomDB: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val albumDataSource: AlbumDataSource
) {

    private val TAG = "AlbumRepositery"
    private var listener: ValueEventListener? = null

    val album: LiveData<List<Album>> = Transformations.map(roomDB.getAllAlbum(fbAuth.currentUser!!.email!!)) {
        it.modifyOverrideAlbumName().asDomainModel()
    }

    suspend fun getAlbum(albumId: String): DatabaseAlbum {
        return roomDB.getAlbum(albumId)
    }


    /**
     * AlbumFragment에서 사용
     * firebase .child("album").child("$uid").child("${album.id}") 경로에
     * FirebaseAlbum객체 write - setValue()
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setValue(album: FirebaseAlbum, albumId: String, setKey : (key:String)->Unit) =
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                val user = fbAuth.currentUser!!
                val ref = fbDatabase.reference.child("album").child(user.uid).child(albumId).push()
                val key = ref.key ?: return@suspendCancellableCoroutine

                val new = FirebaseAlbum(
                    album.id,
                    album.name,
                    album.thumbnail,
                    album.participants,
                    key
                )

                ref.setValue(new).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "ref.setValue(new) Successful")
                        setKey(key)
                        continuation.resume(Unit, null)
                    } else {
                        Log.i(TAG, "ref.setValue(new) fail :${it.exception?.message}")
                        continuation.resume(Unit, null)
                    }

                }
            }catch (error:IOException) {
                error.printStackTrace()
            }catch (error:NullPointerException) {
                error.printStackTrace()
            }

        }

    /**
     * AlbumRequestFramgent에서 사용
     * firebase .child("album").child("$uid").child("${album.id}") 경로에
     * FirebaseAlbum객체 write - setValue()
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setValueInRequestAlbum(album: FirebaseAlbum, albumId: String) =
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                val user = fbAuth.currentUser!!
                val ref = fbDatabase.reference.child("album").child(user.uid).child(albumId).child(album.key)

                val new = FirebaseAlbum(
                    album.id,
                    album.name,
                    album.thumbnail,
                    album.participants,
                    album.key
                )

                ref.setValue(new).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "ref.setValue(new) Successful")
                        continuation.resume(Unit, null)
                    } else {
                        Log.i(TAG, "ref.setValue(new) fail :${it.exception?.message}")
                        continuation.resume(Unit, null)
                    }

                }
            }catch (error:IOException) {
                error.printStackTrace()
            }catch (error:NullPointerException) {
                error.printStackTrace()
            }

        }

    /**
     * firebase .child("album").child("$uid"). child("${album.id}") 경로에
     * ValueEventListener 추가 - addSingleValueEventListener를 이용하여
     * HomeGallery 내 property에 내가 가진 album 리스트 할당하기.
     * */
    suspend fun addSingleValueEventListener(insertAlbum: (album: List<DatabaseAlbum>) -> Unit) {
        withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val ref = fbDatabase.reference.child("album").child(user.uid)
            listener = albumDataSource.getSingleValueEventListener(user.email!!, insertAlbum)
            Log.i(TAG, "addValueEventListener : ref.add~")
            ref.addValueEventListener(listener!!)
        }
    }


    /**
     * firebase .child("album").child("$uid"). child("${album.id}") 경로에
     * ValueEventListener 추가 - addValueEvnetListener
     * */
    suspend fun addValueEventListener(insertAlbum: (albums: List<DatabaseAlbum>) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val user = fbAuth.currentUser!!
                val ref = fbDatabase.reference.child("album").child(user.uid)
                listener = albumDataSource.getValueEventListener(user.email!!, insertAlbum)
                ref.addValueEventListener(listener!!)
            } catch (error: NullPointerException) {
                error.printStackTrace()
            }
        }
    }


    /**
     * room database에 DatabaseAlbum insert
     * */
    suspend fun insertRoomAlbum(album: DatabaseAlbum) {
        withContext(Dispatchers.IO) {
            roomDB.insert(album)
        }
    }

    /**
     * room database에 List<DatabaseAlbum> insert
     * */
    suspend fun insertRoomAlbums(albums: List<DatabaseAlbum>) {
        withContext(Dispatchers.IO) {
            roomDB.insertAlbums(albums)
        }
    }


    /**
     * 앨범이 몇개 있는지 확인.
     * */
    suspend fun addGetAlbumCountSingleValueEventListener(insertCount: (count: Int) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val listener = albumDataSource.getNumberOfAlbumValueEventListener(insertCount)
            val ref = fbDatabase.reference.child("album").child(user.uid)
            ref.addListenerForSingleValueEvent(listener)
            false
        }
    }

    /**
     * remove Listener
     * */
    fun removeListener() {
        try {
            val user = fbAuth.currentUser!!.uid
            val ref = fbDatabase.reference.child("album").child(user)
            ref.removeEventListener(listener!!)

        } catch (error: IOException) {
            error.printStackTrace()
        } catch (error: NullPointerException) {
            error.printStackTrace()
        }
    }
}