package com.ranseo.solaroid.datasource.friend

import android.util.Log
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.firebase.FirebaseDispatchFriend
import com.ranseo.solaroid.firebase.FirebaseFriend
import com.ranseo.solaroid.firebase.asDomainModel
import com.ranseo.solaroid.ui.friend.fragment.add.dispatch.DispatchFriend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FriendCommunicationDataSource() {
    fun getReceptionValueEventListener( listener:(friends:List<Friend>)->Unit ) : ValueEventListener {
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
                listener(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "ValueEventListener onCancelled error : ${error.message}")
            }

        }

        return receptionValueEventListener
    }


    fun getDispatchValueEventListener(listener: (friends:List<DispatchFriend>)->Unit) : ValueEventListener {
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
                listener(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "ValueEventListener onCancelled error : ${error.message}")
            }

        }
        return dispatchValueEventListener
    }



    companion object {
        const val TAG = "프렌드_디스패치_데이터소스"
    }
}