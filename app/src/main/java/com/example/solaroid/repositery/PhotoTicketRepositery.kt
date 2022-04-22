package com.example.solaroid.repositery

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.solaroid.database.DatabasePhotoTicketDao
import com.example.solaroid.database.asDomainModel
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.example.solaroid.firebase.FirebasePhotoTicketContainer
import com.example.solaroid.firebase.asDatabaseModel
import com.example.solaroid.firebase.setPhotoTicketList
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class PhotoTicketRepositery(private val dataSource: DatabasePhotoTicketDao) {

    val photoTickets : LiveData<List<PhotoTicket>> = Transformations.map(dataSource.getAllDatabasePhotoTicket()) {
        it?.let{
            it.asDomainModel()
        }
    }

    suspend fun refreshPhotoTickets(user:FirebaseUser, fbDatabase:FirebaseDatabase) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<FirebasePhotoTicket>()
            val ref = fbDatabase.reference.child("photoTicket").child(user.uid)
            ref.addListenerForSingleValueEvent(setPhotoTicketList(){
                launch(Dispatchers.IO) { dataSource.insert(it.asDatabaseModel()) }
            })
        }
    }

    suspend fun updatePhotoTickets(user:FirebaseUser, fbDatabase:FirebaseDatabase, ) {
        withContext(Dispatchers.IO) {

            val container = FirebasePhotoTicketContainer(list)

            dataSource.insertAll(container.asDatabaseModel())
        }
    }



}