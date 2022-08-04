package com.ranseo.solaroid.ui.friend.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ranseo.solaroid.NavigationViewModel
import com.ranseo.solaroid.NavigationViewModelFactory
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.ActivityFriendBinding
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.room.SolaroidDatabase
import com.google.android.material.navigation.NavigationView

class FriendActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityFriendBinding

    //navController
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    //viewModel
    private lateinit var navigationViewModelFactory: NavigationViewModelFactory
    private lateinit var naviViewModel : NavigationViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val dataSource = SolaroidDatabase.getInstance(this).photoTicketDao
        navigationViewModelFactory = NavigationViewModelFactory(dataSource, this.application )
        naviViewModel = ViewModelProvider(this, navigationViewModelFactory)[NavigationViewModel::class.java]

        binding.naviViewModel = naviViewModel
        binding.lifecycleOwner = this
        //toolbar
        setSupportActionBar(binding.friendToolbar)

        navHostFragment = binding.navHostFragmentFriend.getFragment()
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.fragment_friend_list, R.id.fragment_friend_add), binding.drawerLayoutFriend)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigtionViewFriend.navView.setNavigationItemSelectedListener(this)
        ///

        naviViewModel.naviToHomeAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                navController.navigate(
                    R.id.global_action_friendActivity_to_homeActivity
                )
                this.finish()
            }
        }

        naviViewModel.naviToLoginAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                logout()
                navController.navigate(
                    R.id.global_action_friendActivity_to_loginActivity
                )
                this.finish()
            }
        }

        naviViewModel.naviToFriendAct.observe(this) {
            it.getContentIfNotHandled()?.let{
                navController.navigate(
                    R.id.action_friend_self
                )
                this.finish()
            }
        }


        binding.bottomNaviFriend.setupWithNavController(navController)



    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayoutFriend.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(binding.drawerLayoutFriend.isDrawerOpen(Gravity.LEFT))
            binding.drawerLayoutFriend.closeDrawer(Gravity.LEFT)
        else super.onBackPressed()
    }

    fun logout() {
        FirebaseManager.getAuthInstance().signOut()
    }


    fun setActionBarTitle(str:String) {
        val actionBar = supportActionBar
        actionBar?.title = str
    }

    companion object{
        const val TAG = "프렌드_액티비티"
    }
}