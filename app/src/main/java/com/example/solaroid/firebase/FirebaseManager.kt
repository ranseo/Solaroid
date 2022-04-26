package com.example.solaroid.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FirebaseManager {
    companion object {
        private var FBAUTH: FirebaseAuth? = null
        private var FBDATABASE: FirebaseDatabase? = null
        private var FBSTORAGE : FirebaseStorage? = null


        fun getAuthInstance(): FirebaseAuth {
            synchronized(this) {
                var fbAuth = FBAUTH
                if (fbAuth == null) {
                    fbAuth = Firebase.auth
                }
                FBAUTH = fbAuth
                return fbAuth
            }
        }

        fun getDatabaseInstance(): FirebaseDatabase {
            synchronized(this) {
                var fbDatabase = FBDATABASE
                if(fbDatabase == null) fbDatabase = Firebase.database
                FBDATABASE = fbDatabase
                return fbDatabase
            }
        }

        fun getStorageInstance() : FirebaseStorage {
            synchronized(this) {
                var fbStorage = FBSTORAGE
                if(fbStorage==null) fbStorage = Firebase.storage
                FBSTORAGE = fbStorage
                return fbStorage
            }
        }
    }
}