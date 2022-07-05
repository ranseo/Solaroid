package com.example.solaroid.repositery.album

import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.data.domain.Album
import com.example.solaroid.data.domain.asFirebaseModel
import com.example.solaroid.data.firebase.FirebaseAlbum
import com.example.solaroid.data.room.DatabaseAlbum
import com.example.solaroid.data.room.asDomainModel
import com.example.solaroid.datasource.album.AlbumDataSource
import com.example.solaroid.room.DatabasePhotoTicketDao
import com.example.solaroid.utils.BitmapUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbumRepositery(
    private val roomDB: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val fbDatabase : FirebaseDatabase,
    private val albumDataSource: AlbumDataSource
) {

    val album : LiveData<List<Album>> = Transformations.map(roomDB.getAllAlbum()) {
        it.asDomainModel()
    }


    /**
     * firebase .child("album").child("${album.id}") 경로에
     * FirebaseAlbum객체 write - setValue()
     * */
    suspend fun setValue(album:Album,albumId:String) {
        withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val ref = fbDatabase.reference.child("album").child("${albumId}").push()
            val key = ref.key

//            val firebaseAlbum = FirebaseAlbum(
//                album.id,
//                album.name,
//                BitmapUtils.convertBitmapToByteArray(album.thumbnail),
//                album.participants.asFirebaseModel(),
//                album.photoTickets.asFi
//
//            )

        }
    }


    /**
     * firebase .child("album").child("${album.id}") 경로에
     * ValueEventListener 추가 - addValueEvnetListener
     * */

    suspend fun addValueEventListener(albumId:String, insertAlbum: (album: DatabaseAlbum)->Unit) {
        withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!
            val ref = fbDatabase.reference.child("album").child("${albumId}")
            val listener = albumDataSource.getValueEventListener(user.email!!, insertAlbum)
            ref.addValueEventListener(listener)
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







}