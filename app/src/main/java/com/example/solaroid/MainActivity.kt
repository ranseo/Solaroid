package com.example.solaroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.solaroid.databinding.ActivityMainBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.SolaroidLoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    var isCameraAvailable : Boolean = false


    //Firebase
    private lateinit var viewModel : SolaroidLoginViewModel
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.storage.useEmulator("10.0.2.2", 9199)

        }
        // Request camera permissions
        if (allPermissionsGranted()) {
            isCameraAvailable =true
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        auth = FirebaseManager.getAuthInstance()

        viewModel = ViewModelProvider(this)[SolaroidLoginViewModel::class.java]



        viewModel.authenticationState.observe(this, Observer { state ->
            when(state) {
                SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED-> {
                    Log.i(TAG, "AUTHENTICATED")
                }
                else -> {
                    Log.i(TAG, "NOT AUTHENTICATED")
                    logout()
                }
            }
        })

//        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
//        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.findNavController()
//        binding.botNaivMenu.setupWithNavController(navController)

//        navController.addOnDestinationChan gedListener{_,destination,_ ->
//            if(destination.id == R.id.fragment_solaroid_create || destination.id == R.id.fragment_solaroid_detail) {
//                binding.botNaivMenu.visibility = View.GONE
//            } else {
//                binding.botNaivMenu.visibility = View.VISIBLE
//            }
//        }

//        navController.addOnDestinationChangedListener{_, _, argument ->
//            binding.botNaivMenu.isVisible = argument?.getBoolean("ShowAppBar",false) == true
//        }
//
//
//        navController.addOnDestinationChangedListener{_, destination, _ ->
//            if(destination.id == R.id.fragment_solaroid_create) {
//                if(!isCameraAvailable) {
//                    Toast.makeText(
//                        this,
//                        "해당 기능을 사용하기 위해서는 카메라 허용 승인이 필수적 입니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    onBackPressed()
//                }
//            }
//        }


    }


    private fun logout() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(
            R.id.global_action_mainActivity_to_loginActivity
        )
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(allPermissionsGranted()) {
                isCameraAvailable = true
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
        )== PackageManager.PERMISSION_GRANTED
    }



    companion object {
        const val TAG = "메인액티비티"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).apply {
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


}
