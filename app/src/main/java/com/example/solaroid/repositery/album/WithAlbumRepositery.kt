package com.example.solaroid.repositery.album

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class WithAlbumRepositery(
    private val fbAuth:FirebaseAuth,
    private val fbDatabase:FirebaseDatabase
)
{

    /**
     * firebase .child("withAlbum").child("${album.id}").child("${fbAuth.currentUser.uid}") 경로에 write
     *  setValue(FirebaseProfile())
     * */

    /**
     * firebase .child("withAlbum").child("${album.id}").child("${fbAuth.currentUser.uid}") 경로 read
     *  addSingieValueEventListener - ValueEventListener (한번 읽기)
     * */



}