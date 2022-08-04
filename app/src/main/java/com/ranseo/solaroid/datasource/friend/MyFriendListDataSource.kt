package com.ranseo.solaroid.datasource.friend

import android.util.Log
import com.ranseo.solaroid.models.domain.Friend
import com.ranseo.solaroid.firebase.FirebaseFriend
import com.ranseo.solaroid.firebase.asDomainModel
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.ClassCastException
import java.lang.NullPointerException

class MyFriendListDataSource() {


    fun getFriendListListener(listener: (friend: Friend) -> Unit): ValueEventListener {
        val friendListListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    try {
                        val value = data.value as HashMap<*, *>

                        val friend = FirebaseFriend(
                            id = value["id"]!! as String,
                            nickname = value["nickname"] as String,
                            profileImg = value["profileImg"] as String,
                            friendCode = value["friendCode"] as Long,
                            key = value["key"] as String
                        ).asDomainModel()

                        listener(friend)


                    } catch (error: NullPointerException) {
                        error.printStackTrace()
                    } catch (error: ClassCastException) {
                        error.printStackTrace()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
        return friendListListener
    }


    fun getTmpListener(listener: (friend: Friend) -> Unit): ValueEventListener {
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

                        listener(friend)

                    } catch (error: Exception) {
                        Log.i(TAG, "tmpListListener  error : ${error.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
        return tmpListListener
    }

    suspend fun addFriendListener(
        coroutineScope: CoroutineScope,
        updateFriendList: suspend (friend: List<Friend>) -> Unit
    ): ValueEventListener {

        val addFriendListListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val channel = Channel<Friend>()
                coroutineScope.launch {
                    for (data in snapshot.children) {
                        val hashMap = data.value as HashMap<*, *>
                        try {
                            launch {

                                val friend = FirebaseFriend(
                                    id = hashMap["id"]!! as String,
                                    nickname = hashMap["nickname"] as String,
                                    profileImg = hashMap["profileImg"] as String,
                                    friendCode = hashMap["friendCode"] as Long,
                                    key = hashMap["key"] as String
                                ).asDomainModel()

                                channel.send(friend)
                            }
                        } catch (error: Exception) {
                            Log.d(TAG, "error : ${error.message}")
                        }
                    }

                    var allFriendList = emptyList<Friend>()
                    repeat(snapshot.childrenCount.toInt()) {
                        val friends = channel.receive()
                        allFriendList = (allFriendList + friends)
                        updateFriendList(allFriendList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        return addFriendListListener
    }

    suspend fun addFriendListenerAsync(
        updateFriendList: suspend (friend: List<Friend>) -> Unit
    ): ValueEventListener = coroutineScope {
        val addFriendListListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val channel = Channel<Friend>()
                launch {
                    for (data in snapshot.children) {
                        launch {
                            try {
                                val hashMap = data.value as HashMap<*, *>

                                val friend = FirebaseFriend(
                                    id = hashMap["id"]!! as String,
                                    nickname = hashMap["nickname"] as String,
                                    profileImg = hashMap["profileImg"] as String,
                                    friendCode = hashMap["friendCode"] as Long,
                                    key = hashMap["key"] as String
                                ).asDomainModel()

                                channel.send(friend)
                            } catch (error: Exception) {
                                Log.d(TAG, "error : ${error.message}")
                            }
                        }

                    }

                    var allFriendList = emptyList<Friend>()
                    repeat(snapshot.childrenCount.toInt()) {
                        val friends = channel.receive()
                        allFriendList = (allFriendList + friends)
                        updateFriendList(allFriendList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        addFriendListListener
    }


    companion object {
        const val TAG = "마이프렌드리스트 데이터소스"
    }
}