package com.example.solaroid.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.solaroid.MainActivity
import com.example.solaroid.R
import com.example.solaroid.databinding.ActivityLoginBinding
import com.example.solaroid.firebase.FirebaseManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
        Firebase.database.useEmulator("10.0.2.2", 9000)
        Firebase.storage.useEmulator("10.0.2.2", 9199)


    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseManager.getAuthInstance().currentUser
        if(user!=null && user.isEmailVerified) {
//            val navHostFragment = supportFragmentManager.findFragmentById(R.id.login_navigation) as NavHostFragment
//            navHostFragment.navController.navigate(
//                SolaroidLoginFragmentDirections.actionLoginNavigationToMainNavigation()
//            )
            //finish()
        }
    }
}


