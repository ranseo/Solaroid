package com.example.solaroid.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.solaroidframe.SolaroidFrameFragmentContainer
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealTimeDatabaseViewModel(user: FirebaseUser?) : ViewModel() {
    private val firebaseUser = user
    private val db = Firebase.database
    val ref = db.reference

    private val _photoTickets = MutableLiveData<List<PhotoTicket>?>()
    val photoTickets: LiveData<List<PhotoTicket>?>
        get() = _photoTickets


    init {
        if(firebaseUser != null)
            setPhotoTicketList(ref.child("photoTicket").child("${firebaseUser.uid}"))
    }

    fun setValueInPhotoTicket(photoTicket: PhotoTicket) {
        ref.child("photoTicket").child(firebaseUser!!.uid).push().setValue(photoTicket)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(
                        TAG,
                        "firebase 실시간 데이터베이스로 데이터 전송. firebase Database : ${db}"
                    )
                } else {
                    Log.i(
                        TAG,
                        "firebase 실시간 데이터베이스로 데이터 전송 실패.",
                        it.exception
                    )
                }
            }
    }

    fun setPhotoTicketList(reference: DatabaseReference) {
        viewModelScope.launch {
            val photoTicketList = mutableListOf<PhotoTicket>()
            val ins = async {
                withContext(Dispatchers.IO) {

                    val photoTicketListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (list in snapshot.children) {
                                val hashMap = list.value as HashMap<String, *>

                                val phototicket = PhotoTicket(date = hashMap["date"]!! as String, photo = hashMap["photo"]!! as String, id = hashMap["id"]!! as Long, frontText = hashMap["frontText"]!! as String, backText = hashMap["backText"]!! as String, favorite = hashMap["favorite"]!! as Boolean)
                                Log.i(TAG,"${phototicket}")
                                photoTicketList.add(phototicket)


                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    }

                    reference.addListenerForSingleValueEvent(photoTicketListener)
                }
            }
            if (ins.await() == Unit) {
                _photoTickets.value = photoTicketList
            }
        }


    }


    companion object {
        const val TAG = "리얼타임데이터베이스"
    }

}