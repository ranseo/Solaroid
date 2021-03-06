package com.example.solaroid.datasource.friend

import android.util.Log
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.firebase.FirebaseDispatchFriend
import com.example.solaroid.firebase.FirebaseFriend
import com.example.solaroid.firebase.asDomainModel
import com.example.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FriendCommunicationDataSource(
    val listener: OnDataListener

) {
    interface OnDataListener {
        fun onReceptionDataChanged(friend:List<Friend>)
        fun onDispatchDataChanged(friend:List<DispatchFriend>)
    }


    val receptionValueEventListener = object:ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val list = mutableListOf<Friend>()
            for(data in snapshot.children) {
                val hashMap = data.value as HashMap<*,*>

                val friend = FirebaseFriend(
                    hashMap["id"]!! as String,
                    hashMap["nickname"]!! as String,
                    hashMap["profileImg"]!! as String,
                    hashMap["friendCode"]!! as Long,
                    hashMap["key"]!! as String
                ).asDomainModel()

                list += listOf(friend)
            }
            listener.onReceptionDataChanged(list)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i(TAG, "ValueEventListener onCancelled error : ${error.message}")
        }

    }

    val dispatchValueEventListener = object:ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val list = mutableListOf<DispatchFriend>()
            for(data in snapshot.children) {
                val hashMap = data.value as HashMap<*,*>

                val friend = FirebaseDispatchFriend(
                    hashMap["flag"] as String,
                    hashMap["id"]!! as String,
                    hashMap["nickname"]!! as String,
                    hashMap["profileImg"]!! as String,
                    hashMap["friendCode"]!! as Long,
                ).asDomainModel()

                list += listOf(friend)
            }
            listener.onDispatchDataChanged(list)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i(TAG, "ValueEventListener onCancelled error : ${error.message}")
        }

    }

    companion object {
        const val TAG = "?????????_????????????_???????????????"
    }
}