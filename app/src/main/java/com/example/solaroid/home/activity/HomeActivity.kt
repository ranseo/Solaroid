package com.example.solaroid.home.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.solaroid.BuildConfig
import com.example.solaroid.NavigationViewModel
import com.example.solaroid.R
import com.example.solaroid.databinding.ActivityHomeBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.SolaroidLoginViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var isCameraAvailable: Boolean = false


    private lateinit var binding: ActivityHomeBinding

    //viewModel
    private lateinit var viewModel: SolaroidLoginViewModel
    private lateinit var naviViewModel: NavigationViewModel

    //Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseDatabase

    //toolbar
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.storage.useEmulator("10.0.2.2", 9199)

        }

        navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        navController = navHostFragment.navController


        // Request camera permissions
        if (allPermissionsGranted()) {
            isCameraAvailable = true
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        auth = FirebaseManager.getAuthInstance()

        //LoginViewModel
        viewModel = ViewModelProvider(this)[SolaroidLoginViewModel::class.java]
        //NavigationViewModel
        naviViewModel = ViewModelProvider(this)[NavigationViewModel::class.java]
        binding.naviViewModel = naviViewModel
        binding.lifecycleOwner = this

        viewModel.authenticationState.observe(this, Observer { state ->
            when (state) {
                SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.i(TAG, "AUTHENTICATED")
                    viewModel.isProfileSet()
                }
                else -> {
                    Log.i(TAG, "NOT AUTHENTICATED")
                    logout()
                }
            }
        })

        viewModel.naviToNext.observe(this) { event ->
            event.getContentIfNotHandled()?.let{
                if(!it) {
                       navController.navigate(
                           R.id.global_action_mainActivity_to_loginActivity
                       )
                }
            }
        }


        //toolbar
        setSupportActionBar(binding.mainToolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_solaroid_frame_container,
                R.id.fragment_solaroid_gallery
            ), binding.drawerLayoutMain
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.naviationViewMain.navView.setNavigationItemSelectedListener(this)

        //
        navController.addOnDestinationChangedListener{_,destination,_ ->
            val id = destination.id
            if(id== R.id.fragment_solaroid_gallery || id== R.id.fragment_solaroid_frame_container) supportActionBar?.show()
            else supportActionBar?.hide()
        }


    }

    override fun onSupportNavigateUp() : Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_info -> {
                auth.signOut()
            }
        }
        binding.drawerLayoutMain.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayoutMain.isDrawerOpen(Gravity.LEFT))
            binding.drawerLayoutMain.closeDrawer(Gravity.LEFT)
        else  super.onBackPressed()
    }


    private fun logout() {
        navController.navigate(
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
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
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
        ) == PackageManager.PERMISSION_GRANTED
    }




    companion object {
        const val TAG = "메인액티비티"
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
