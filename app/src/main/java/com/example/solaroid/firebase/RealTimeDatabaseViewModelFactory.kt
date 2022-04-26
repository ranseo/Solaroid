//package com.example.solaroid.firebase
//
//import android.app.Application
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.google.firebase.auth.FirebaseUser
//import java.lang.IllegalArgumentException
//
//class RealTimeDatabaseViewModelFactory(val user: FirebaseUser?, val application: Application) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        @Suppress("UNCHECKED_CAST")
//        if(modelClass.isAssignableFrom(RealTimeDatabaseViewModel::class.java)) {
//            return RealTimeDatabaseViewModel(user, application) as T
//        }
//        throw IllegalArgumentException("UNKNOWN_CLASS")
//    }
//}