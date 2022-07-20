package com.example.solaroid.repositery.album

import android.util.Log
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.FirebaseRequestAlbum
import com.example.solaroid.parseProfileImgStringToList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class AlbumRequestRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val requestAlbumDataSource: RequestAlbumDataSource
) {
    private val TAG = "AlbumRequestRepositery"
    private var listener: ValueEventListener? = null


    /**
     * firebase .child("albumRequest").child("${friendCode}") 경로에 write
     *  setValue("${Album().Id}")
     * */
    suspend fun setValueToParticipants(request: FirebaseRequestAlbum) =
        suspendCancellableCoroutine<Unit> { continuation ->
            val list = parseProfileImgStringToList(request.participants)
            for (friendCode in list) {
                val ref =
                    fbDatabase.reference.child("albumRequest").child(friendCode.drop(1)).push()
                val key = ref.key!!

                val firebaseRequestAlbum = FirebaseRequestAlbum(
                    request.id,
                    request.name,
                    request.thumbnail,
                    request.participants,
                    key
                )

                ref.setValue(firebaseRequestAlbum).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.i(TAG,"각 참여자들에게 Request value 쓰기 성공")
                        continuation.resume(Unit,null)
                    }else {
                        Log.d(TAG,"setValue 실패 ${it.exception?.message}.")
                        continuation.resume(Unit,null)
                    }
                }
            }

        }


    /**
     * firebase .child("albumRequest").child("${my.friendCode}") 경로 read
     *  addSingleValueEventListener() : ValueEventListener
     * */
    fun addValueEventListener(myFriendCode: Long, insert: (requests: List<RequestAlbum>) -> Unit) {
        listener = requestAlbumDataSource.getValueEventListener(insert)
        val ref = fbDatabase.reference.child("albumRequest").child("$myFriendCode")
        ref.addValueEventListener(listener!!)
    }

    /**
     * firebase.child("albumRequest").child("${my.friendCode}") 경로 상의 데이터 delete
     * removeValue() 호출
     * */
    fun deleteValue(myFriendCode: Long, key: String) {
        val ref = fbDatabase.reference.child("albumRequest").child("${myFriendCode}").child(key)
        ref.removeValue()
    }


    /**
     * listener 제거
     */
    fun removeListener(myFriendCode: String) {
        val ref = fbDatabase.reference.child("albumRequest").child("$myFriendCode")
        ref.removeEventListener(listener!!)
    }

}