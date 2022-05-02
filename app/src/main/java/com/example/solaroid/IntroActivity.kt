package com.example.solaroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.solaroid.databinding.ActivityIntroBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class IntroActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }

        auth = FirebaseManager.getAuthInstance()

        val user = auth.currentUser

        if (user == null || user.isEmailVerified) {
            startLoginActivity()
        } else startMainActivity()

        binding.introImage.playAnimation()
    }

    fun startMainActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            startActivity(Intent(this@IntroActivity, MainActivity::class.java))
            finish()
        }
    }

    fun startLoginActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            finish()
        }
    }


}