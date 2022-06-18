package com.example.solaroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.databinding.ActivitySplashBinding
import com.example.solaroid.home.activity.HomeActivity
import com.example.solaroid.login.LoginActivity
import com.example.solaroid.login.LoginViewModelFactory
import com.example.solaroid.login.SolaroidLoginViewModel
import com.example.solaroid.room.SolaroidDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SplashActivity : AppCompatActivity() {
    var isCameraAvailable: Boolean = false

    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivitySplashBinding

    private lateinit var viewModelFactory: LoginViewModelFactory
    private lateinit var viewModel : SolaroidLoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.storage.useEmulator("10.0.2.2", 9199)

        }


            if (allPermissionsGranted()) {
                isCameraAvailable = true
                splashScreen()
            } else {
                ActivityCompat.requestPermissions(
                    this@SplashActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )

            }




        val dataSource = SolaroidDatabase.getInstance(this).photoTicketDao
        viewModelFactory = LoginViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this,viewModelFactory)[SolaroidLoginViewModel::class.java]


    }


    fun splashScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.authenticationState.observe(this) {
                when(it) {
                    SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED -> {
                        startActivity(Intent(this,HomeActivity::class.java))
                        finish()
                    }
                    else -> {
                        startActivity(Intent(this,LoginActivity::class.java))
                        finish()
                    }
                }

            }
        }, 1000)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                isCameraAvailable = true
                splashScreen()
            } else {
                Toast.makeText(
                    this,
                    "카메라 승인 요청에 거부됐습니다.\n해당 어플리케이션의 일부 기능을 사용하기 위해서는 카메라 허용이 필수적 입니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }



    companion object {
        const val TAG = "스플래쉬_액티비티"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}