package com.example.solaroid.repositery.album

import com.example.solaroid.datasource.album.RequestAlbumDataSource
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.FirebaseRequestAlbum
import com.example.solaroid.parseProfileImgStringToList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbumRequestRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val requestAlbumDataSource: RequestAlbumDataSource
) {

    private var listener: ValueEventListener? = null


    /**
     * firebase .child("albumRequest").child("${friendCode}") 경로에 write
     *  setValue("${Album().Id}")
     * */
    suspend fun setValueToParticipants(request: RequestAlbum) {
        withContext(Dispatchers.IO) {
            val list = parseProfileImgStringToList(request.participant)
            for (friendCode in list) {
                val ref = fbDatabase.reference.child("albumRequest").child("$friendCode").push()
                val key = ref.key!!

                val firebaseRequestAlbum = FirebaseRequestAlbum(
                    request.id,
                    request.name,
                    request.participant,
                    key
                )

                ref.setValue(firebaseRequestAlbum)
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
     * listener 제거
     */
    fun removeListener(myFriendCode: String) {
        val ref = fbDatabase.reference.child("albumRequest").child("$myFriendCode")
        ref.removeEventListener(listener!!)
    }

}