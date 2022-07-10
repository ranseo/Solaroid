package com.example.solaroid.ui.home.fragment.gallery

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.databinding.FragmentGalleryBinding
import com.example.solaroid.dialog.FilterDialogFragment
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView

class GalleryFragment : Fragment(), FilterDialogFragment.OnFilterDialogListener {

    private lateinit var viewModelFactory: GalleryViewModelFactory
    private lateinit var viewModel: GalleryViewModel
    private lateinit var binding: FragmentGalleryBinding

    private lateinit var filterDialogFragment: FilterDialogFragment

    private val args by navArgs<GalleryFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        val albumId = args.albumId

        viewModelFactory = GalleryViewModelFactory(dataSource.photoTicketDao, application, albumId)
        viewModel = ViewModelProvider(this, viewModelFactory)[GalleryViewModel::class.java]

        val adapter = SolaroidGalleryAdapter(OnClickListener { photoTicket ->
            viewModel.navigateToFrame(photoTicket)
        })

        binding.photoTicketRec.adapter = adapter

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            list?.let {
                Log.i(TAG, "viewModel.photoTickets.observe(viewLifecycleOwner) { list -> ${list} }")
                adapter.submitList(list)
            }
        }

        navgiateToOtherFragment()


        setOnItemSelectedListener(binding.galleryBottomNavi)
        filterDialogFragment = FilterDialogFragment(this)
        return binding.root

    }

    /**
     * gallery Fragment에서 viewModel naviTo 라이브 객체를 관찰하여
     * 원하는 Fragment로 이동하기 위한 코드를 모아놓은 함수
     * */
    private fun navgiateToOtherFragment() {
        viewModel.naviToFrame.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { photoTicket->
                val filter = viewModel.filter.value?.filter ?: "DESC"
                findNavController().navigate(
                    GalleryFragmentDirections.actionGalleryToFrame(
                        filter,
                        photoTicket
                    )
                )
            }
        }

        viewModel.naviToAdd.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { id ->
                findNavController().navigate(
                    GalleryFragmentDirections.actionGalleryToAdd(id)
                )
            }
        }

        viewModel.naviToCreate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { id ->
                findNavController().navigate(
                    GalleryFragmentDirections.actionGalleryToCreate(id)
                )
            }
        }

        viewModel.naviToHome.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{
                findNavController().navigate(
                    GalleryFragmentDirections.actionGalleryToHome()
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
                    viewModel.navigateToHome()
                    true
                }
                R.id.album -> {
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
        super.onDestroy()
        viewModel.removeListener()
    }


    companion object {
        const val TAG = "갤러리프래그먼트"
    }
}
