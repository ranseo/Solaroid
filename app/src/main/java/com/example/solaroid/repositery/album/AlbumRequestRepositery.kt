package com.example.solaroid.repositery.album

import android.util.Log
import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.FirebaseRequestAlbum
import com.example.solaroid.parseProfileImgStringToList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.NullPointerException

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
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setValueToParticipants(participants:List<Friend>, request: FirebaseRequestAlbum) =
        suspendCancellableCoroutine<Unit> { continuation ->
            for (p in participants) {
                val ref =
                    fbDatabase.reference.child("albumRequest").child(p.friendCode.drop(1)).push()
                val key = ref.key!!

                val firebaseRequestAlbum = FirebaseRequestAlbum(
                    request.id,
                    request.name,
                    request.thumbnail,
                    request.participants,
                    key
                )

                ref.setValue(firebaseRequestAlbum).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "각 참여자들에게 Request value 쓰기 성공")
                        continuation.resume(Unit, null)
                    } else {
                        Log.d(TAG, "setValue 실패 ${it.exception?.message}.")
                        continuation.resume(Unit, null)
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
        try {
            val ref = fbDatabase.reference.child("albumRequest").child("$myFriendCode")
            ref.removeEventListener(listener!!)
        }catch (error:NullPointerException) {
            error.printStackTrace()
        }catch(error:IOException) {
            error.printStackTrace()
        }
    }

}