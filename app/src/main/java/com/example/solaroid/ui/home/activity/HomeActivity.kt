package com.example.solaroid.ui.home.activity

import android.os.Bundle
import android.util.Log
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
import com.example.solaroid.ui.login.viewmodel.LoginViewModelFactory
import com.example.solaroid.ui.login.viewmodel.SolaroidLoginViewModel
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

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
        navigationViewModelFactory = NavigationViewModelFactory(dataSource, this.application)
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
            setOf(R.id.fragment_home_gallery, R.id.fragment_album, R.id.fragment_gallery) ,binding.drawerLayoutMain
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.naviationViewMain.navView.setNavigationItemSelectedListener(this)


        //
//        navController.addOnDestinationChangedListener{_,destination,_ ->
//            val id = destination.id
//            if(id== R.id.fragment_solaroid_gallery || id== R.id.fragment_solaroid_frame) supportActionBar?.show()
//            else supportActionBar?.hide()
//        }

        navController.addOnDestinationChangedListener{ _,_,arguments ->
            if(arguments != null) {
                if(arguments.containsKey("ShowAppBar")) supportActionBar?.show()
                else supportActionBar?.hide()
            } else supportActionBar?.hide()

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

//        naviViewModel.naviToAlbumAct.observe(this) {
//            it.getContentIfNotHandled()?.let{
//                navController.navigate(
//                    R.id.global_action_homeActivity_to_albumActivity
//                )
//                this.finish()
//            }
//        }

        naviViewModel.naviToHomeAct.observe(this){
            it.getContentIfNotHandled()?.let{
                navController.navigate(
                    R.id.action_home_self
                )
                this.finish()
            }
        }


        naviViewModel.myProfile.observe(this) {
            if(it==null) {
                Log.i(TAG,"naviViewModel.myProfile.observe")
                naviViewModel.insertProfileRoomDatabase()
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
        if (binding.drawerLayoutMain.isDrawerOpen(GravityCompat.START))
            binding.drawerLayoutMain.closeDrawer(GravityCompat.START)
        else  super.onBackPressed()
    }


    private fun logout() {
        auth.signOut()
    }



    companion object {
        const val TAG = "메인액티비티"
    }


}
