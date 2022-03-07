package com.example.solaroid.solaroidframe

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.example.solaroid.databinding.FragmentSolaroidFrameFilterBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenuItemView
import com.google.android.material.navigation.NavigationBarView

class SolaroidFrameLately() : SolaroidFrameFragmentFilter() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameFilterBinding>(
            inflater,
            R.layout.fragment_solaroid_frame_filter,
            container,
            false
        )
        val application: Application = requireNotNull(this.activity).application
        val dataSource: SolaroidDatabase = SolaroidDatabase.getInstance(application)

        val viewModel = ViewModelProvider(
            requireParentFragment(),
            SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        )[SolaroidFrameViewModel::class.java]
        val adapter = SolaroidFrameAdapter(OnClickListener {
            viewModel.navigateToDetail(it)
        })

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewpager.adapter = adapter


        registerOnPageChangeCallback(viewModel, binding.viewpager, adapter)
        observePhotoTicket(viewModel)
        observeFavorite(viewModel, binding.fragmentFrameBottomNavi)


        setOnItemSelectedListener(viewModel, binding.fragmentFrameBottomNavi, binding.viewpager)

        return binding.root
    }

    override fun setOnItemSelectedListener(
        viewModel: SolaroidFrameViewModel,
        botNavi: BottomNavigationView,
        viewPager: ViewPager2
    ) {

        botNavi.setOnItemSelectedListener {
            if (it.itemId == R.id.favorite) {
                val favorite = viewModel.favorite.value
                if (favorite != null) {
                    if (favorite) {
                        viewModel.togglePhotoTicketFavorite(false)
                    } else viewModel.togglePhotoTicketFavorite(
                        true
                    )
                }
            } else if (it.itemId == R.id.edit) {
                viewModel.navigateToEdit(viewModel.photoTicket.value?.id)
            }
            false
        }

    }
}


class SolaroidFrameFavorite() : SolaroidFrameFragmentFilter() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameFilterBinding>(
            inflater,
            R.layout.fragment_solaroid_frame_filter,
            container,
            false
        )
        val application: Application = requireNotNull(this.activity).application
        val dataSource: SolaroidDatabase = SolaroidDatabase.getInstance(application)


        val viewModel = ViewModelProvider(
            requireParentFragment(),
            SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        )[SolaroidFrameViewModel::class.java]

        val adapter = SolaroidFrameAdapter(OnClickListener {
            viewModel.navigateToDetail(it)
        })

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewpager.adapter = adapter


        registerOnPageChangeCallback(viewModel, binding.viewpager, adapter)
        observePhotoTicket(viewModel)
        observeFavorite(viewModel, binding.fragmentFrameBottomNavi)
        observeCurrentPosition(viewModel, adapter)

        setOnItemSelectedListener(viewModel, binding.fragmentFrameBottomNavi, binding.viewpager)



        return binding.root
    }

    private fun observeCurrentPosition(
        viewModel: SolaroidFrameViewModel,
        adapter: SolaroidFrameAdapter
    ) {
        viewModel.currentPosition.observe(viewLifecycleOwner, Observer { it ->
            if (it >= 0 && it < adapter.itemCount) {
                val photoTicket = adapter.getPhotoTicket(it)
                photoTicket?.let { it2 ->
                    viewModel.setCurrentPhotoTicket(it2)
                }
            } else {
                viewModel.setCurrentFavorite(false)
                viewModel.setCurrentPhotoTicket(null)
            }
        })
    }

    override fun setOnItemSelectedListener(
        viewModel: SolaroidFrameViewModel,
        botNavi: BottomNavigationView,
        viewPager: ViewPager2
    ) {
        botNavi.setOnItemSelectedListener {
            if (it.itemId == R.id.favorite) {

                viewModel.offPhotoTicketFavorite(false)
                val position = viewPager.currentItem + 1

                Log.d("프레임프래그먼트", "position : ${position}")
                viewModel.setCurrentPosition(position)


            } else if (it.itemId == R.id.edit) {
                viewModel.navigateToEdit(viewModel.photoTicket.value?.id)
            }
            false
        }
    }
}