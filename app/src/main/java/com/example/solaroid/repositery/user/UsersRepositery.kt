package com.example.solaroid.repositery.user

import android.util.Log
import com.example.solaroid.firebase.FirebaseProfile
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class UsersRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase,
    private val fbStorage: FirebaseStorage
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun updateAllUserNum(num: Long) {
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Unit> { continuation ->
                fbDatabase.reference.child("currentUsersNum").setValue(num).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "fun updateAllUserNum success")
                        continuation.resume(Unit, null)
                    } else {
                        Log.i(TAG, "fun updateAllUserNum fail")
                        continuation.resume(Unit, null)
                    }
                }
            }
        }
    }

    suspend fun getAllUserNum(): Task<DataSnapshot>? {
        return withContext(Dispatchers.IO) {
            val numRef = fbDatabase.reference.child("currentUsersNum").get()

            numRef
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun insertUsersList(profile: FirebaseProfile) {
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Unit> { continuation ->
                val usersRef = fbDatabase.reference.child("allUsers").child("${profile.friendCode}")

                usersRef.setValue(profile).addOnCompleteListener {
                    if (it.isSuccessful) {
                        continuation.resume(Unit, null)
                        Log.i(TAG, "fun insertUsersList success")
                    } else {
                        continuation.resume(Unit, null)
                        Log.i(TAG, "fun insertUsersList fail")
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "유저 리포지터리"
    }


}