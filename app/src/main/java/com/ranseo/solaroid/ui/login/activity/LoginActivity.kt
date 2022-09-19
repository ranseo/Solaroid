package com.ranseo.solaroid.ui.login.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ranseo.solaroid.databinding.ActivityLoginBinding
import com.ranseo.solaroid.firebase.FirebaseManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData : Uri? = intent.data
        if(Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.lastPathSegment?.also { invite ->

            }
        }

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

    override fun onDestroy() {
        val currentUser = FirebaseManager.getAuthInstance().currentUser
        if(currentUser != null) {
            if(!currentUser.isEmailVerified) FirebaseManager.getAuthInstance().signOut()
        }

        super.onDestroy()
    }
}


