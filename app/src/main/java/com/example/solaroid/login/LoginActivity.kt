package com.example.solaroid.login

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.solaroid.MainActivity
import com.example.solaroid.databinding.ActivityLoginBinding
import com.example.solaroid.firebase.FirebaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseManager.getAuthInstance().currentUser
        if(user !=null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}


