package com.example.solaroid.home.activity

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.solaroid.NavigationViewModel
import com.example.solaroid.NavigationViewModelFactory
import com.example.solaroid.R
import com.example.solaroid.databinding.ActivityHomeBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.LoginViewModelFactory
import com.example.solaroid.login.SolaroidLoginViewModel
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var isCameraAvailable: Boolean = false


    private lateinit var binding: ActivityHomeBinding

    //viewModel
    private lateinit var viewModelFactory : LoginViewModelFactory
    private lateinit var viewModel: SolaroidLoginViewModel

    private lateinit var navigationViewModelFactory: NavigationViewModelFactory
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
//        if (BuildConfig.DEBUG) {
//            Firebase.auth.useEmulator("10.0.2.2", 9099)
//            Firebase.database.useEmulator("10.0.2.2", 9000)
//            Firebase.storage.useEmulator("10.0.2.2", 9199)
//        }

        navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        navController = navHostFragment.navController


        auth = FirebaseManager.getAuthInstance()

        val dataSource = SolaroidDatabase.getInstance(this).photoTicketDao
        //LoginViewModel
        viewModelFactory = LoginViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this,viewModelFactory)[SolaroidLoginViewModel::class.java]

        //NavigationViewModel
        navigationViewModelFactory = NavigationViewModelFactory(dataSource)
        naviViewModel = ViewModelProvider(this,navigationViewModelFactory)[NavigationViewModel::class.java]
        binding.naviViewModel = naviViewModel
        binding.lifecycleOwner = this

        viewModel.authenticationState.observe(this, Observer { state ->
            when (state) {
                SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.i(TAG, "AUTHENTICATED")

                }
                else -> {
                    Log.i(TAG, "NOT AUTHENTICATED")
                    navController.navigate(
                        R.id.global_action_homeActivity_to_loginActivity
                    )
                    finish()
                }
            }
        })

        viewModel.myProfile.observe(this) {
            it?.let{
                Log.i(TAG,"myProfile : ${it}")
            }
        }


        viewModel.naviToNext.observe(this) { event ->
            event.getContentIfNotHandled()?.let{
                if(!it) {
                       navController.navigate(
                           R.id.global_action_homeActivity_to_loginActivity
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

        naviViewModel.naviToLoginAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                logout()
            }
        }

        naviViewModel.naviToFriendAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                navController.navigate(
                    R.id.global_action_homeActivity_to_friendActivity
                )
                this.finish()
            }
        }

    }

    override fun onSupportNavigateUp() : Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.login_info -> {
//                auth.signOut()
//            }
//        }
        binding.drawerLayoutMain.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayoutMain.isDrawerOpen(Gravity.LEFT))
            binding.drawerLayoutMain.closeDrawer(Gravity.LEFT)
        else  super.onBackPressed()
    }


    private fun logout() {
        auth.signOut()
    }



    companion object {
        const val TAG = "메인액티비티"
    }


}
