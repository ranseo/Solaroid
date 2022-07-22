package com.example.solaroid.repositery.album

import android.util.Log
import com.example.solaroid.datasource.album.WithAlbumDataSource
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.firebase.FirebaseProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class WithAlbumRepositery(
    private val fbAuth:FirebaseAuth,
    private val fbDatabase:FirebaseDatabase,
    private val withAlbumDataSource: WithAlbumDataSource
)
{
    private val TAG = "WithAlbumRepositery"

    /**
     * firebase .child("withAlbum").child("${album.id}").child("${fbAuth.currentUser.uid}") 경로에 write
     *  setValue(FirebaseProfile())
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setValue(myProfile:FirebaseProfile, albumId:String) = suspendCancellableCoroutine<Unit>{ continuation ->
            val user = fbAuth.currentUser!!

            val ref = fbDatabase.reference.child("withAlbum").child(albumId).child(user.uid)

            ref.setValue(myProfile).addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.i(TAG, "setValue 성공")
                    continuation.resume(Unit, null)
                } else {
                    Log.i(TAG, "setValue 실패 : ${it.exception?.message}")
                    continuation.resume(Unit, null)
                }
            }
    }


    /**
     * firebase .child("withAlbum").child("${album.id}").child("${fbAuth.currentUser.uid}") 경로 read
     *  addSingieValueEventListener - ValueEventListener (한번 읽기)
     * */
    suspend fun addValueEventListener(albumId:String) {
        withContext(Dispatchers.IO) {
            val listener = withAlbumDataSource.getWithAlbumListener()
            val ref = fbDatabase.reference.child("withAlbum").child("$albumId")

            ref.addListenerForSingleValueEvent(listener)
        }
    }




}