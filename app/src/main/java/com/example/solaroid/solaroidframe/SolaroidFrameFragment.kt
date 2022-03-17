package com.example.solaroid.solaroidframe

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.OnFrameLongClickListener
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameFilterBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SolaroidFrameLately() : SolaroidFrameFragmentFilter() {

    private lateinit var viewModel: SolaroidFrameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("프레임", "레이틀리 onCreate()")
    }

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

        viewModel = ViewModelProvider(
            requireParentFragment(),
            SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        )[SolaroidFrameViewModel::class.java]
        val adapter = SolaroidFrameAdapter(OnFrameLongClickListener {
                showListDialog(viewModel)
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

    private lateinit var viewModel: SolaroidFrameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("프레임", "페이보릿 onCreate()")
    }

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


        viewModel = ViewModelProvider(
            requireParentFragment(),
            SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        )[SolaroidFrameViewModel::class.java]

        val adapter = SolaroidFrameAdapter(OnFrameLongClickListener {
            showListDialog(viewModel)
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