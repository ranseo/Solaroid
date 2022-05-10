package com.example.solaroid.solaroidframe


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.NavigationViewModel
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.example.solaroid.firebase.FirebaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlin.math.log

open class SolaroidFrameFragmentContainer : Fragment(), PopupMenu.OnMenuItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val TAG = "프레임 컨테이너"
        const val TAG_L = "LATELY"
        const val TAG_F = "FAVORITE"

    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    private lateinit var naviViewModel : NavigationViewModel

    private lateinit var binding: FragmentSolaroidFrameContainerBinding

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.commit { add(R.id.fragment_frame_container_view, SolaroidFrameFragment(), TAG) }

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_solaroid_frame_container,
                R.id.fragment_solaroid_gallery
            ), binding.drawerLayout
        )

        toolbar = binding.frameCotainerToolbar

        toolbar.setupWithNavController(navController, appBarConfiguration)

        toolbar.inflateMenu(R.menu.fragment_frame_toolbar_menu)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.move_gallery_fragment -> {
                    viewModel.navigateToGallery()
                    true
                }
                R.id.filter -> {
                    viewModel.onFilterPopupMenu()
                    true
                }
                else -> false
            }

        }
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

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)


        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this.requireActivity(), viewModelFactory)[SolaroidFrameViewModel::class.java]

        naviViewModel = ViewModelProvider(this.requireActivity())[NavigationViewModel::class.java]


        binding.viewModel = viewModel
        binding.naviViewModel = naviViewModel
        binding.lifecycleOwner = viewLifecycleOwner



        naviViewModel.naviToLoginAct.observe(viewLifecycleOwner, Observer{
            it.getContentIfNotHandled()?.let{
                logout()
            }
        })

        viewModel.popUpMenu.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                popupShow(binding.frameCotainerToolbar.findViewById(R.id.filter))
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

            else -> true
        }
    }


    private fun setNavigationViewListener() {
        binding.navView.navView.setNavigationItemSelectedListener(this)
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

    private fun logout() {
        FirebaseManager.getAuthInstance().signOut()
    }
}