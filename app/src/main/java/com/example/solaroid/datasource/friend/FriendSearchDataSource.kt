package com.example.solaroid.datasource.friend

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FriendSearchDataSource(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase
) {
    fun getTask(friendCode: Long) : Task<DataSnapshot> {
        return fbDatabase.reference.child("allUsers").child("${friendCode}").get()
    }
}