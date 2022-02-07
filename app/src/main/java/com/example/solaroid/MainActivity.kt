package com.example.solaroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.solaroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


    }
}