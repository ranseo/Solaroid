package com.example.solaroid.solaroidframe


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.example.solaroid.firebase.RealTimeDatabaseViewModel
import com.example.solaroid.firebase.RealTimeDatabaseViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

open class SolaroidFrameFragmentContainer : Fragment(), PopupMenu.OnMenuItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val TAG = "프레임 컨테이너"
        const val TAG_L = "LATELY"
        const val TAG_F = "FAVORITE"

    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    private lateinit var binding: FragmentSolaroidFrameContainerBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.commit { add(R.id.fragment_add_container_view, SolaroidFrameFragment(), TAG) }

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_solaroid_frame_container,
                R.id.fragment_solaroid_gallery
            ), binding.drawerLayout
        )

        binding.frameCotainerToolbar.setupWithNavController(navController, appBarConfiguration)
        setNavigationViewListener()


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSolaroidFrameContainerBinding>(
            inflater,
            R.layout.fragment_solaroid_frame_container,
            container,
            false
        )


        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)


        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.popUpMenu.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                popupShow(binding.popupMenuFilter)
            }
        })


        viewModel.naviToCreateFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToCreateFragment()
                )
            }
        })


        viewModel.naviToEditFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { key ->
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToEditFragment(
                        key
                    )
                )
            }
        })


        viewModel.naviToAddFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToAddFragment()
                )
            }
        })


        viewModel.naviToGallery.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToGalleryFragment()
                )
            }
        })


        return binding.root
    }


    private fun popupShow(view: View) {
        val popUp = PopupMenu(this.activity, view)
        popUp.setOnMenuItemClickListener(this@SolaroidFrameFragmentContainer)
        popUp.menuInflater.inflate(R.menu.fragment_frame_popup_menu, popUp.menu)
        popUp.show()
    }


    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        return when (p0?.itemId) {
            R.id.filter_lately -> {
                viewModel.setPhotoTicketFilter(PhotoTicketFilter.LATELY)
                true
            }
            R.id.filter_favorite -> {
                viewModel.setPhotoTicketFilter(PhotoTicketFilter.FAVORITE)
                true
            }
            R.id.login_info -> {
                viewModel.logout()
                true
            }
            else -> true
        }
    }


    private fun setNavigationViewListener() {
        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_info -> {
                viewModel.logout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}