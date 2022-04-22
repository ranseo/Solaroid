package com.example.solaroid.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 * firebase database reference photoTicket/user.uid 내에 있는 FirebasePhotoTicket 들을
 * ValueEventListener를 통해 읽어들이고, 이 데이터들을 list<FirebasePhotoTicket>로 만들어서 리턴.
 * */
suspend fun setPhotoTicketList(
    insert: (FirebasePhotoTicket) -> Unit
) : ValueEventListener {
    withContext(Dispatchers.IO) {
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (list in snapshot.children) {
                    val hashMap = list.value as HashMap<*, *>

                    val phototicket = FirebasePhotoTicket(
                        url = hashMap["url"]!! as String,
                        path = hashMap["path"]!! as String,
                        date = hashMap["date"]!! as String,
                        frontText = hashMap["frontText"]!! as String,
                        backText = hashMap["backText"]!! as String,
                        favorite = hashMap["favorite"]!! as Boolean
                    )

                    insert(phototicket)

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
    }
}


