package com.example.solaroid.album.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.solaroid.NavigationViewModel
import com.example.solaroid.NavigationViewModelFactory
import com.example.solaroid.R
import com.example.solaroid.databinding.ActivityAlbumBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class AlbumActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityAlbumBinding

    //firebase
    private lateinit var fbAuth : FirebaseAuth

    //navController
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    //NaviViewModel
    private lateinit var naviViewModel : NavigationViewModel
    private lateinit var naviViewModelFactory : NavigationViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fbAuth = FirebaseManager.getAuthInstance()

        val dataSource = SolaroidDatabase.getInstance(this.application).photoTicketDao

        naviViewModelFactory = NavigationViewModelFactory(dataSource, this.application)
        naviViewModel = ViewModelProvider(this,naviViewModelFactory)[NavigationViewModel::class.java]

        binding.naviViewModel = naviViewModel
        binding.lifecycleOwner = this


        //toolbar
        setSupportActionBar(binding.albumToolbar)

        navHostFragment = binding.navHostFragment.getFragment()
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayoutAlbum)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationViewAlbum.navView.setNavigationItemSelectedListener(this)



        naviViewModel.naviToHomeAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                navController.navigate(
                    R.id.global_action_albumAct_to_homeAct
                )
                this.finish()
            }
        }

        naviViewModel.naviToLoginAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                logout()
                navController.navigate(
                    R.id.global_action_albumAct_to_loginAct
                )
                this.finish()
            }
        }

        naviViewModel.naviToFriendAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                logout()
                navController.navigate(
                    R.id.global_action_albumAct_to_friendAct
                )
                this.finish()
            }
        }

        naviViewModel.naviToAlbumAct.observe(this){
            it.getContentIfNotHandled()?.let{
                navController.navigate(
                    R.id.action_album_self
                )
                this.finish()

            }
        }

    }

    override fun onSupportNavigateUp() : Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout() {
        fbAuth.signOut()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayoutAlbum.closeDrawer(GravityCompat.START)
        return true
    }


}
