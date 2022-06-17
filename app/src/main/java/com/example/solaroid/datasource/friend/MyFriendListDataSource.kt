package com.example.solaroid.datasource.friend

import android.util.Log
import com.example.solaroid.domain.Friend
import com.example.solaroid.firebase.FirebaseFriend
import com.example.solaroid.firebase.asDomainModel
import com.google.firebase.database.*

class MyFriendListDataSource(
    private var listener: OnValueListener
) {
    interface OnValueListener {
        fun onValueAdded(friend: Friend)
        fun onValueRemoved(friend: Friend)
        fun onValueChanged(friend: Friend)
    }

    val friendListListener: ChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val hashMap = snapshot.value as HashMap<*, *>

            try {
                val friend = FirebaseFriend(
                    id = hashMap["id"]!! as String,
                    nickname = hashMap["nickname"] as String,
                    profileImg = hashMap["profileImg"] as String,
                    friendCode = hashMap["friendCode"] as Long,
                    key = hashMap["key"] as String
                ).asDomainModel()

                listener.onValueAdded(friend)

            } catch (error: Exception) {
                Log.i(TAG, "friendListListener error : ${error.message}")
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

    val tmpListListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (data in snapshot.children) {

                val hashMap = data.value as HashMap<*, *>

                try {
                    val friend = FirebaseFriend(
                        id = hashMap["id"]!! as String,
                        nickname = hashMap["nickname"] as String,
                        profileImg = hashMap["profileImg"] as String,
                        friendCode = hashMap["friendCode"] as Long,
                        key = hashMap["key"] as String
                    ).asDomainModel()

                    listener.onValueChanged(friend)

                } catch (error: Exception) {
                    Log.i(TAG, "tmpListListener  error : ${error.message}")
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }

    }

    companion object {
        const val TAG = "마이프렌드리스트 데이터소스"
    }
}