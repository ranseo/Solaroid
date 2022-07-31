package com.example.solaroid.datasource.friend

import android.util.Log
import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.asDomainModel
import com.example.solaroid.ui.friend.fragment.add.FriendAddViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendSearchDataSource() {
    private val TAG = "FriendSearchDataSource"

    fun getValueEventListener(listenerNull: (profile:Profile?)->Unit, listenerSet : (profile: Profile)->Unit) : ValueEventListener {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {

                    val hashMap = snapshot.value as HashMap<*, *>

                    val profile = FirebaseProfile(
                        hashMap["id"]!! as String,
                        hashMap["nickname"]!! as String,
                        hashMap["profileImg"]!! as String,
                        hashMap["friendCode"]!! as Long

                    ).asDomainModel()


                    listenerNull(profile)
                    listenerSet(profile)

                } catch (error: Exception) {
                    listenerNull(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
               listenerNull(null)
                Log.i(TAG, "task is fail")
            }
        }

        return valueEventListener
    }

}