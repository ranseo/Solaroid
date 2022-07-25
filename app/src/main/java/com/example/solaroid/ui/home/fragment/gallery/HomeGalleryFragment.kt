package com.example.solaroid.ui.home.fragment.gallery

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.databinding.FragmentSolaroidGalleryBinding
import com.example.solaroid.dialog.FilterDialogFragment
import com.example.solaroid.parseAlbumIdDomainToFirebase
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeGalleryFragment : Fragment(), FilterDialogFragment.OnFilterDialogListener {

    private lateinit var viewModelFactory: HomeGalleryViewModelFactory
    private lateinit var viewModel: HomeGalleryViewModel
    private lateinit var binding: FragmentSolaroidGalleryBinding

    private lateinit var filterDialogFragment: FilterDialogFragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_solaroid_gallery, container, false)
        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = HomeGalleryViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeGalleryViewModel::class.java]

        val adapter = SolaroidGalleryAdapter(OnClickListener { photoTicket ->
            viewModel.navigateToFrame(photoTicket)
        })

        binding.photoTicketRec.adapter = adapter

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.albums.observe(viewLifecycleOwner) { list ->
            list?.let{
                for(album in list) {
                    viewModel.refreshFirebaseListener(parseAlbumIdDomainToFirebase(album.id,album.key), album.key)
                }
            }
        }

        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            list?.let {
                Log.i(TAG, "viewModel.photoTickets.observe(viewLifecycleOwner) { list -> ${list} }")
                adapter.submitList(list)
            }
        }



        navgiateToOtherFragment()

        //binding.galleryBottomNavi.setupWithNavController(findNavController())
        setOnItemSelectedListener(binding.galleryBottomNavi)
        binding.galleryBottomNavi.itemIconTintList = null

        filterDialogFragment = FilterDialogFragment(this)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        binding.galleryBottomNavi.menu.findItem(R.id.home).isChecked = true
    }

    /**
     * gallery Fragment에서 viewModel naviTo 라이브 객체를 관찰하여
     * 원하는 Fragment로 이동하기 위한 코드를 모아놓은 함수
     * */
    private fun navgiateToOtherFragment() {
        viewModel.naviToFrame.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { photoTicket ->
                val filter = viewModel.filter.value?.filter ?: "DESC"
                val albumId = photoTicket.albumInfo[0]
                val albumKey = photoTicket.albumInfo[1]
                Log.i(TAG, "albumId : ${albumId}, albumKey: $albumKey")
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToFrame(
                        filter,
                        photoTicket,
                        albumId,
                        albumKey
                    )
                )
            }
        }

        viewModel.naviToAdd.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToAdd()
                )
            }
        }

        viewModel.naviToCreate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToCreate()
                )
            }
        }

        viewModel.naviToAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToAlbum()
                )
            }
        }


    }


    private fun showFilterDialog() {
        filterDialogFragment.show(parentFragmentManager, "filterDialog")
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_gallery_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter -> {
                showFilterDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


    private fun setOnItemSelectedListener(
        botNavi: BottomNavigationView
    ) {
        botNavi.setOnItemSelectedListener { it ->
            when (it.itemId) {
                R.id.home -> {

                    true
                }
                R.id.album -> {
                    viewModel.navigateToAlbum()
                    true
                }

                R.id.add -> {
                    viewModel.navigateToAdd()
                    true

                }
                else -> false
            }
        }
    }

    override fun onFilterDesc() {
        viewModel.setFilter("DESC")
    }

    override fun onFilterAsc() {
        viewModel.setFilter("ASC")
    }

    override fun onFilterFavorite() {
        viewModel.setFilter("FAVORITE")
    }

    override fun onDestroy() {
        viewModel.removeListener()
        super.onDestroy()
    }


    companion object {
        const val TAG = "갤러리프래그먼트"
    }
}
