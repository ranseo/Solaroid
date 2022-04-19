package com.example.solaroid.firebase

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealTimeDatabaseViewModel(user: FirebaseUser?, application: Application) : AndroidViewModel(application) {
    private val firebaseUser = user
    private val db = Firebase.database
    val ref = db.reference

    private val _photoTickets = MutableLiveData<List<PhotoTicket>?>()
    val photoTickets: LiveData<List<PhotoTicket>?>
        get() = _photoTickets


    init {
        Log.i(TAG, "TAG Init")
        if(firebaseUser != null)
            setPhotoTicketList(ref.child("photoTicket").child("${firebaseUser.uid}"))
    }

    fun setValueInPhotoTicket(photoTicket: PhotoTicket) {
        ref.child("photoTicket").child(firebaseUser!!.uid).push().setValue(photoTicket, DatabaseReference.CompletionListener{databaseError, databaseReference ->
            if(databaseError != null) {
                Log.d(TAG, "Unable to write Message to database",databaseError.toException())
                return@CompletionListener
            }

            val key = databaseReference.key
            val storageReference = Firebase.storage
                .getReference(firebaseUser!!.uid)
                .child(key!!)
                .child(photoTicket.photo.toUri().lastPathSegment!!)

            val mimeType : String? = photoTicket.photo.toUri()?.let{
                getApplication<Application>().contentResolver.getType(it)
            }
            Log.i(TAG, "photoTicket.photo : ${photoTicket.photo}\ntoUri() : ${photoTicket.photo.toUri()}\nlastPathSegment : ${photoTicket.photo.toUri().lastPathSegment!!}")

            putImageInStorage(storageReference, photoTicket.photo.toUri(), key, photoTicket)
        })


//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Log.i(
//                        TAG,
//                        "firebase 실시간 데이터베이스로 데이터 전송. firebase Database : ${db}"
//                    )
//                } else {
//                    Log.i(
//                        TAG,
//                        "firebase 실시간 데이터베이스로 데이터 전송 실패.",
//                        it.exception
//                    )
//                }
//            }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String, _photoTicket: PhotoTicket) {
        // Upload the image to Cloud Storage
        Log.i(TAG, "putImageInStorage")
        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnashot ->
                Log.i(TAG, "Success")
                taskSnashot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.i(TAG,"uri : ${uri}")
                        val photoTicket = PhotoTicket(_photoTicket.id, uri.toString(), _photoTicket.frontText , _photoTicket.backText, _photoTicket.date, _photoTicket.favorite)
                        db.reference.child("photoTicket")
                            .child(firebaseUser!!.uid)
                            .child(key)
                            .setValue(photoTicket)
                    }
            }
            .addOnFailureListener { e->
                Log.w(TAG,
                    "Image upload task was unsuccessful.",
                    e)
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
                                val hashMap = list.value as HashMap<*, *>

                                val phototicket = PhotoTicket(date = hashMap["date"]!! as String, photo = hashMap["photo"]!! as String, id = hashMap["id"]!! as Long, frontText = hashMap["frontText"]!! as String, backText = hashMap["backText"]!! as String, favorite = hashMap["favorite"]!! as Boolean)

                                photoTicketList.add(phototicket)
                                _photoTickets.value = photoTicketList
                                Log.i(TAG,"${phototicket}, photoTicketsValue = ${photoTickets.value}")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    }

                    reference.addListenerForSingleValueEvent(photoTicketListener)
                }
            }
        }
    }

    companion object {
        const val TAG = "리얼타임데이터베이스"
    }

}