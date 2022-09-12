package com.ranseo.solaroid.repositery.log

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogRepositery(val fbDatabase: FirebaseDatabase) {

    suspend fun sendLog(log: String) {
        withContext(Dispatchers.IO) {
            fbDatabase.reference.child("log").child("login").setValue(log)
        }
    }


    suspend fun sendLogToAlbumCreate(uid:String, log:String) {
        withContext(Dispatchers.IO) {
            fbDatabase.reference.child("log").child(uid).child("album").child("create").setValue(log)
        }
    }
}