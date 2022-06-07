package com.example.solaroid.datasource

import android.util.Log
import com.example.solaroid.firebase.FirebaseProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class MyFriendListDataSource(
    private var listener : OnValueListener
) {
    interface OnValueListener {
        fun onValueAdded(profile: FirebaseProfile)
        fun onValueRemoved(profile: FirebaseProfile)
    }

    val friendListListener: ChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val hashMap = snapshot.value as HashMap<*, *>

                try {
                    val profile = FirebaseProfile(
                        id = hashMap["id"]!! as String,
                        nickname = hashMap["nickname"] as String,
                        profileImg = hashMap["profileMap"] as String,
                        friendCode = hashMap["friendCode"] as Long
                    )

                    if(listener!=null) listener!!.onValueAdded(profile)
                    else Log.i(TAG,"listener is null")
                } catch (error:Exception) {
                    Log.i(TAG,"profile error : ${error.message}")
                }

        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }
    companion object {
        const val TAG = "마이프렌드리스트 데이터소스"
    }
}