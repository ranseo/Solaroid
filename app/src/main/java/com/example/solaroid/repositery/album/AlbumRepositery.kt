package com.example.solaroid.repositery.album

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.models.room.asDomainModel
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.models.firebase.asDatabaseModel
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.utils.BitmapUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AlbumRepositery(
    private val roomDB: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val fbDatabase : FirebaseDatabase,
    private val albumDataSource: AlbumDataSource
) {

    private val TAG = "AlbumRepositery"
    private var listener : ValueEventListener? = null

    val album : LiveData<List<Album>> = Transformations.map(roomDB.getAllAlbum()) {
        it.asDomainModel()
    }

    suspend fun getAlbum(albumId:String) : DatabaseAlbum{
        return roomDB.getAlbum(albumId)
    }


    /**
     * firebase .child("album").child("$uid").child("${album.id}") 경로에
     * FirebaseAlbum객체 write - setValue()
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setValue(album:FirebaseAlbum, albumId:String) = suspendCancellableCoroutine<Unit>{ continuation ->
            val user = fbAuth.currentUser!!
            val ref = fbDatabase.reference.child("album").child(user.uid).child("$albumId").push()
            val key = ref.key ?: return@suspendCancellableCoroutine

            val new = FirebaseAlbum(
                album.id,
                album.name,
                album.thumbnail,
                album.participants,
                key
            )

            ref.setValue(new).addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.i(TAG,"ref.setValue(new) Successful")
                } else {
                    Log.i(TAG,"ref.setValue(new) fail :${it.exception?.message}")
                }
                continuation.resume(Unit,null)
            }
    }

    /**
     * firebase .child("album").child("$uid").child("${album.id}") 경로에
     * FirebaseAlbum객체 write - setValue()
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setValueInProfile(album:FirebaseAlbum, albumId:String, insertRoom : (databaseAlbum:DatabaseAlbum)->Unit) = suspendCancellableCoroutine<Unit>{ continuation ->
        val user = fbAuth.currentUser!!
        val ref = fbDatabase.reference.child("album").child(user.uid).child("$albumId").push()
        val key = ref.key ?: return@suspendCancellableCoroutine

        val new = FirebaseAlbum(
            album.id,
            album.name,
            album.thumbnail,
            album.participants,
            key
        )

        ref.setValue(new).addOnCompleteListener {
            if(it.isSuccessful) {
                insertRoom(new.asDatabaseModel())
                Log.i(TAG,"ref.setValue(new) Successful")
            } else {
                Log.i(TAG,"ref.setValue(new) fail :${it.exception?.message}")
            }
            continuation.resume(Unit,null)
        }
    }


    /**
     * firebase .child("album").child("$uid"). child("${album.id}") 경로에
     * ValueEventListener 추가 - addValueEvnetListener
     * */

    suspend fun addValueEventListener(insertAlbum: (album: DatabaseAlbum)->Unit) {
        withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val ref = fbDatabase.reference.child("album").child(user.uid)
            listener = albumDataSource.getValueEventListener(insertAlbum)
            ref.addValueEventListener(listener!!)
        }
    }



    /**
     * room database에 DatabaseAlbum insert
     * */
    suspend fun insertRoomAlbum(album:DatabaseAlbum) {
        withContext(Dispatchers.IO) {
            roomDB.insert(album)
        }
    }


    /**
     * 앨범이 몇개 있는지 확인.
     * */
    suspend fun addGetAlbumCountSingleValueEventListener(insertCount:(count:Int)->Unit) : Boolean {
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
        val user = fbAuth.currentUser!!.uid
        val ref = fbDatabase.reference.child("album").child(user)
        ref.removeEventListener(listener!!)
    }
}