package com.ranseo.solaroid.repositery.album

import android.util.Log
import com.ranseo.solaroid.datasource.album.WithAlbumDataSource
import com.ranseo.solaroid.models.firebase.FirebaseAlbum
import com.ranseo.solaroid.models.firebase.FirebaseProfile
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
    suspend fun setValue(myProfile:FirebaseProfile, albumId:String, albumKey:String) = suspendCancellableCoroutine<Unit>{ continuation ->
            val user = fbAuth.currentUser!!

            val ref = fbDatabase.reference.child("withAlbum").child(albumId).child(user.uid).child(albumKey)

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
    suspend fun addValueEventListener(albumId:String, albumKey:String) {
        withContext(Dispatchers.IO) {
            val user = fbAuth.currentUser!!.uid!!
            val listener = withAlbumDataSource.getWithAlbumListener()
            val ref = fbDatabase.reference.child("withAlbum").child(albumId).child(user).child(albumKey)

            ref.addListenerForSingleValueEvent(listener)
        }
    }

    /**
     * album을 삭제하면 firebase withAlbum - albumId - uid -> removeValue
     * */
    suspend fun removeWithAlbumValue(album:FirebaseAlbum) {
        return withContext(Dispatchers.IO){
            val user = fbAuth.currentUser!!
            fbDatabase.reference.child("withAlbum").child(album.id).child(user.uid).child(album.key).removeValue()
        }
    }

}