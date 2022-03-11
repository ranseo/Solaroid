package com.example.solaroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.solaroid.databinding.ActivityMainBinding
import com.example.solaroid.dialog.SaveDialogFragment

class MainActivity : AppCompatActivity() {
    var isCameraAvailable : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            isCameraAvailable =true
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()
        binding.botNaivMenu.setupWithNavController(navController)

//        navController.addOnDestinationChangedListener{_,destination,_ ->
//            if(destination.id == R.id.fragment_solaroid_create || destination.id == R.id.fragment_solaroid_detail) {
//                binding.botNaivMenu.visibility = View.GONE
//            } else {
//                binding.botNaivMenu.visibility = View.VISIBLE
//            }
//        }

        navController.addOnDestinationChangedListener{_, _, argument ->
            binding.botNaivMenu.isVisible = argument?.getBoolean("ShowAppBar",false) == true
        }


        navController.addOnDestinationChangedListener{_, destination, _ ->
            if(destination.id == R.id.fragment_solaroid_create) {
                if(!isCameraAvailable) {
                    Toast.makeText(
                        this,
                        "해당 기능을 사용하기 위해서는 카메라 허용 승인이 필수적 입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBackPressed()
                }
            }
        }


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
