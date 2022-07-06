package com.example.solaroid.datasource.album

import com.example.solaroid.models.domain.Profile
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.firebase.FirebaseProfile
import com.example.solaroid.models.firebase.asDomainModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class WithAlbumDataSource {
    fun getWithAlbumListener() : ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profiles = mutableListOf<Profile>()
                for(data in snapshot.children) {
                    val hashMap = data.value as HashMap<*,*>

                    val profile = FirebaseProfile(
                        id = hashMap["id"] as String,
                        nickname = hashMap["nickname"] as String,
                        profileImg = hashMap["profileImg"] as String,
                        friendCode = hashMap["friendCode"] as Long
                    ).asDomainModel()

                    profiles.add(profile)

                }


            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
    }

}