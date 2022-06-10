package com.example.solaroid.repositery

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendCommunicateRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase
) {
    suspend fun addValueListenerToDisptachRef(listener: ValueEventListener) {
        return withContext(Dispatchers.IO) {
            try {
                val user = fbAuth.currentUser
                fbDatabase.reference.child("friendDispatch").child("${user!!.uid}").child("list")
                    .addValueEventListener(listener)
            } catch (e: Exception) {
                Log.i(TAG, "addValueListenerToDisptachRef error : ${e.message}")
            }
        }
    }

    suspend fun addValueListenerToReceptionRef(friendCode:Long ,listener: ChildEventListener) {
        return withContext(Dispatchers.IO) {
            try {
                val user = fbAuth.currentUser
                fbDatabase.reference.child("friendReception").child("${friendCode}").child("list")
                    .addChildEventListener(listener)
            } catch (e: Exception) {
                Log.i(TAG, "addValueListenerToReceptionRef error : ${e.message}")
            }
        }
    }


    companion object {
        const val TAG = "프렌드_커뮤니케이션_리포지터리"
    }
}