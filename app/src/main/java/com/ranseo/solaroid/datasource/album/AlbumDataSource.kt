package com.ranseo.solaroid.datasource.album

import android.util.Log
import com.ranseo.solaroid.models.firebase.FirebaseAlbum
import com.ranseo.solaroid.models.firebase.asDatabaseModel
import com.ranseo.solaroid.models.room.DatabaseAlbum
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AlbumDataSource {

    private val TAG = "AlbumDataSource"

    fun getValueEventListener(
        user: String,
        insertAlbum: (albums: List<DatabaseAlbum>) -> Unit
    ): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val hash = data.value as HashMap<*, *>

                    val albums = hash.values.map { v ->
                        val hashMap = v as HashMap<*, *>
                        val album = FirebaseAlbum(
                            id = hashMap["id"] as String,
                            name = hashMap["name"] as String,
                            thumbnail = hashMap["thumbnail"] as String,
                            participants = hashMap["participants"] as String,
                            numOfParticipants = hashMap["numOfParticipants"] as Long,
                            key = hashMap["key"] as String
                        ).asDatabaseModel(user)

                        album
                    }

                    insertAlbum(albums)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    fun getSingleValueEventListener(
        user: String,
        insertAlbum: (albums: List<DatabaseAlbum>) -> Unit
    ): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG,"onDataChange : start")
                try {
                    for (data in snapshot.children) {
                        val hash = data.value as HashMap<*, *>

                        val albums = hash.values.map { v ->
                            val hashMap = v as HashMap<*, *>
                            val album = FirebaseAlbum(
                                id = hashMap["id"] as String,
                                name = hashMap["name"] as String,
                                thumbnail = hashMap["thumbnail"] as String,
                                participants = hashMap["participants"] as String,
                                numOfParticipants = hashMap["numOfParticipants"] as Long,
                                key = hashMap["key"] as String
                            ).asDatabaseModel(user)

                            album
                        }

                        Log.i(TAG,"onDataChange : ${albums.isEmpty()}")
                        if (albums.isEmpty()) insertAlbum(listOf())
                        else insertAlbum(albums)
                    }
                    insertAlbum(listOf())
                } catch (error: Exception) {
                    Log.d(TAG, "error: ${error.printStackTrace()}")
                    insertAlbum(listOf())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    fun getNumberOfAlbumValueEventListener(insertCount: (count: Int) -> Unit): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cnt = snapshot.childrenCount

                insertCount(cnt.toInt())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
    }
}