package com.ranseo.solaroid.repositery.profile

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileImageRepositery(
    val fbAuth: FirebaseAuth,
    val fbStorage: FirebaseStorage
) {


    suspend fun getMetaDataFromProfileStorage(lambda:(value:Int)->Unit)  {
        val uid = fbAuth.currentUser?.uid ?: return

        withContext(Dispatchers.IO) {
            val ref = fbStorage.getReference("${PATH}${uid}")
            ref.metadata.addOnSuccessListener { metadata ->
                val value=metadata.getCustomMetadata("orientation")?.toInt() ?: -1
                lambda(value)
            }.addOnFailureListener {  error ->
                Log.e(TAG,"ref.metadata fail : ${error.message}")
                lambda(-1)

            }
        }
    }


    companion object {
        private const val PATH = "profile/"
        private const val TAG = "ProfileImageRepositery"
    }
}