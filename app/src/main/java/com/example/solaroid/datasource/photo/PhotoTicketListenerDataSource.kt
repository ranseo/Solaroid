package com.example.solaroid.datasource.photo

import android.util.Log
import com.example.solaroid.firebase.FirebasePhotoTicket
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PhotoTicketListenerDataSource {


    suspend fun setPhotoTicketList(
        insertRoomDb:  (List<FirebasePhotoTicket>) -> Unit
    ): ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

               val photoTickets = snapshot.children.map { list ->
                   val hashMap = list.value as HashMap<*, *>
                   val phototicket = FirebasePhotoTicket(
                       url = hashMap["url"]!! as String,
                       key = hashMap["key"]!! as String,
                       date = hashMap["date"]!! as String,
                       frontText = hashMap["frontText"]!! as String,
                       backText = hashMap["backText"]!! as String,
                       favorite = hashMap["favorite"]!! as Boolean
                   )

                   phototicket
               }
                Log.i(TAG, "photoTicket : ${photoTickets}")

               insertRoomDb(photoTickets)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }


    companion object {
        const private val TAG = "포토티켓_리스너_데이터소스"
    }


}