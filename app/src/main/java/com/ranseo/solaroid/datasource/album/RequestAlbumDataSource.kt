package com.ranseo.solaroid.datasource.album

import com.ranseo.solaroid.models.domain.RequestAlbum
import com.ranseo.solaroid.models.firebase.FirebaseRequestAlbum
import com.ranseo.solaroid.models.firebase.asDomainModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RequestAlbumDataSource {
    fun getValueEventListener(insert: (requests : List<RequestAlbum>)->Unit) : ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = mutableListOf<RequestAlbum>()
                for(data in snapshot.children) {
                    val hashMap = data.value as HashMap<*,*>

                    val requestAlbum = FirebaseRequestAlbum(
                        id = hashMap["id"] as String,
                        name = hashMap["name"] as String,
                        thumbnail = hashMap["thumbnail"] as String,
                        participants = hashMap["participants"] as String,
                        numOfParticipants =   hashMap["numOfParticipants"] as Long,
                        albumKey = hashMap["albumKey"] as String,
                        key = hashMap["key"] as String
                    ).asDomainModel()

                    requests += requestAlbum
                }

                insert(requests)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
    }
}