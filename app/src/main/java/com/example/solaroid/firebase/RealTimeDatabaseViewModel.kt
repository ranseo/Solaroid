package com.example.solaroid.firebase

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.solaroidframe.SolaroidFrameFragmentContainer
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RealTimeDatabaseViewModel : ViewModel() {
    private val db = Firebase.database
    val ref = db.reference

    fun setValueInPhotoTicket(photoTicket: PhotoTicket, user:FirebaseUser) {
        ref.child("photoTicket").child(user.uid).push().setValue(photoTicket).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(
                    TAG,
                    "firebase 실시간 데이터베이스로 데이터 전송. firebase Database : ${db}"
                )
            } else {
                Log.i(TAG,
                    "firebase 실시간 데이터베이스로 데이터 전송 실패.",
                    it.exception
                )
            }
        }
    }




    companion object{
        const val TAG = "리얼타임데이터베이스"
    }

}