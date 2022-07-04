package com.example.solaroid.repositery.album

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AlbumRepositery(
    private val fbAuth: FirebaseAuth,
    private val fbDatabase : FirebaseDatabase
) {

    /**
     * firebase .child("album").child("${album.id}") 경로에
     * FirebaseAlbum객체 write - setValue()
     * */



    /**
     * firebase .child("album").child("${album.id}") 경로에
     * ValueEventListener 추가 - addValueEvnetListener
     * */




}