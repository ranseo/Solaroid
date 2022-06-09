package com.example.solaroid.repositery

import android.util.Log
import com.example.solaroid.datasource.FriendSearchDataSource
import com.example.solaroid.domain.Profile
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendAddRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase
) {

    suspend fun addSearchListener(friendCode: Long, listener:ValueEventListener) {
        return withContext(Dispatchers.IO) {
            fbDatabase.reference.child("allUsers").child("${friendCode}").addListenerForSingleValueEvent(listener)
        }
    }

    suspend fun setValueToFriendReception()

    companion object {
        const val TAG = "프렌드 애드 리포지터리"
    }

}