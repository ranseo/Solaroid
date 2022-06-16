package com.example.solaroid.home.fragment.gallery

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidGalleryBinding

class SolaroidGalleryFragment : Fragment() {

    private lateinit var viewModelFactory: SolaroidGalleryViewModelFactory
    private lateinit var viewModel: SolaroidGalleryViewModel
    private lateinit var binding: FragmentSolaroidGalleryBinding


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

        viewModelFactory = SolaroidGalleryViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidGalleryViewModel::class.java]

        val adapter = SolaroidGalleryAdapter(OnClickListener { photoTicketKey ->
            viewModel.naviToDetail(photoTicketKey)
        })

        binding.photoTicketRec.adapter = adapter

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.navigateToDetailFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidGalleryFragmentDirections.actionGalleryFragmentToDetailFragment(it)
                )
            }
        })

        viewModel.naviToFrame.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidGalleryFragmentDirections.actionGalleryFragmentToFrameFragment()
                )
            }
        })


        return binding.root

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_gallery_toolbar_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.move_frame_fragment -> {
                Log.i(TAG, "main_frame_fragment")
                viewModel.navigateToFrame()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }



    companion object {
        const val TAG = "갤러리프래그먼트"
    }
}
