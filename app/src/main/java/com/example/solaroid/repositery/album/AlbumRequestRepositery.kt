package com.example.solaroid.repositery.album

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AlbumRequestRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase: FirebaseDatabase
) {
    /**
     * firebase .child("albumRequest").child("${myProfile.friendCode}") 경로에 write
     *  setValue("${Album().Id}")
     * */

    /**
     * firebase .child("albumRequest").child("${myProfile.friendCode}") 경로 read
     *  addSingleValueEventListener() : ValueEventListener
     * */

}