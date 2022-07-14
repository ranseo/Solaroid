package com.example.solaroid.datasource.album

import com.example.solaroid.models.firebase.FirebaseAlbum
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.asDatabaseModel
import com.example.solaroid.models.room.DatabaseAlbum
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AlbumDataSource  {

    fun getValueEventListener(insertAlbum : (album: DatabaseAlbum) -> Unit) : ValueEventListener{
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children){
                    val hashMap = data.value as HashMap<*,*>

                    val album = FirebaseAlbum(
                        id = hashMap["id"] as String,
                        name = hashMap["name"] as String,
                        thumbnail= hashMap["thumbnail"] as String,
                        participants = hashMap["participants"] as String,
                        key = hashMap["key"] as String
                    ).asDatabaseModel()

                    insertAlbum(album)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
    }

    fun getNumberOfAlbumValueEventListener(insertCount : (count:Int)->Unit) : ValueEventListener {
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