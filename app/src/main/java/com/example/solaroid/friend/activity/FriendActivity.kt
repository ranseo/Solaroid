package com.example.solaroid.friend.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.solaroid.R
import com.example.solaroid.databinding.ActivityFriendBinding
import com.google.android.material.navigation.NavigationView

class FriendActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityFriendBinding
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding = ActivityFriendBinding.inflate(layoutInflater)

        //toolbar
        setSupportActionBar(binding.friendToolbar)

        navHostFragment = binding.navHostFragmentFriend.getFragment()
        navController = navHostFragment.navController


        val appBarConfiguration = AppBarConfiguration(setOf(), binding.drawerLayoutFriend)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigtionViewFriend.navView.setNavigationItemSelectedListener(this)
        ///




    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_info -> {

            }
        }
        binding.drawerLayoutFriend.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(binding.drawerLayoutFriend.isDrawerOpen(Gravity.LEFT))
            binding.drawerLayoutFriend.closeDrawer(Gravity.LEFT)
        else super.onBackPressed()
    }
}