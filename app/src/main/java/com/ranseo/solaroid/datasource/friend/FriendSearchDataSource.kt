package com.ranseo.solaroid.datasource.friend

import android.util.Log
import com.ranseo.solaroid.models.domain.Profile
import com.ranseo.solaroid.models.firebase.FirebaseProfile
import com.ranseo.solaroid.models.firebase.asDomainModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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