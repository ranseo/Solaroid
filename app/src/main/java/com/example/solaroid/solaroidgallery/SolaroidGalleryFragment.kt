package com.example.solaroid.solaroidgallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidGalleryBinding
import com.google.android.material.navigation.NavigationView

class SolaroidGalleryFragment :Fragment(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var viewModelFactory: SolaroidGalleryViewModelFactory
    private lateinit var viewModel : SolaroidGalleryViewModel
    private lateinit var binding : FragmentSolaroidGalleryBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.fragment_solaroid_frame_container,R.id.fragment_solaroid_gallery), binding.drawerLayout)

        binding.galleryToolbar.setupWithNavController(navController, appBarConfiguration)
        setNavigationViewListener()
    }

    private fun setNavigationViewListener() {
        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.login_info -> {
                Log.i("프레임컨테이너", "login_info")
                viewModel.logout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSolaroidGalleryBinding>(inflater,R.layout.fragment_solaroid_gallery, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = SolaroidGalleryViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidGalleryViewModel::class.java]

        val adapter = SolaroidGalleryAdapter(OnClickListener { photoTicketKey ->
            viewModel.naviToDetail(photoTicketKey)
        })

        binding.photoTicketRec.adapter = adapter

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

//        viewModel.navigateToCreateFrag.observe(viewLifecycleOwner, Observer {
//            if(it) {
//                findNavController().navigate(
//                    SolaroidGalleryFragmentDirections.actionGalleryFragmentToCreateFragment()
//                )
//                viewModel.doneNaviToCreateFrag()
//            }
//        })


        viewModel.navigateToDetailFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidGalleryFragmentDirections.actionGalleryFragmentToDetailFragment(it)
                )
            }
        })

        viewModel.naviToFrame.observe(viewLifecycleOwner, Observer{
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidGalleryFragmentDirections.actionGalleryFragmentToFrameFragment()
                )
            }
        })


        return binding.root

    }
}