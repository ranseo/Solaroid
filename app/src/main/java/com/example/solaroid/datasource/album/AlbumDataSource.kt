package com.example.solaroid.datasource.album

import com.example.solaroid.data.firebase.FirebaseAlbum
import com.example.solaroid.data.firebase.FirebaseProfile
import com.example.solaroid.data.firebase.asDatabaseModel
import com.example.solaroid.data.room.DatabaseAlbum
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AlbumDataSource  {

    fun getValueEventListener(user:String, insertAlbum : (album: DatabaseAlbum) -> Unit) : ValueEventListener{
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children){
                    val hashMap = data.value as HashMap<*,*>

                    val album = FirebaseAlbum(
                        id = hashMap["id"] as String,
                        name = hashMap["name"] as String,
                        thumbnail= hashMap["thumbnail"] as ByteArray,
                        participants = hashMap["participants"] as List<FirebaseProfile>,
                        photoTickets = hashMap["photoTickets"] as List<FirebasePhotoTicket>,
                        key = hashMap["key"] as String
                    ).asDatabaseModel(user)

                    insertAlbum(album)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
    }
}