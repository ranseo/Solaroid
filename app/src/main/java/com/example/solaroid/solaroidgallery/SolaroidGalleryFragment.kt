package com.example.solaroid.solaroidgallery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.convertPhotoTicketToToastString
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidGalleryBinding

class SolaroidGalleryFragment :Fragment() {

    private lateinit var viewModelFactory: SolaroidGalleryViewModelFactory
    private lateinit var viewModel : SolaroidGalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidGalleryBinding>(inflater,R.layout.fragment_solaroid_gallery, container, false)

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


        //제대로 사진이 생성됐는지 확인하기 위해 PhotoTicket이 만들어질 때 Toast 를 생성.
        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let{
                Toast.makeText(context, convertPhotoTicketToToastString(it,this.resources), Toast.LENGTH_LONG).show()
                viewModel.doneToToast()
            }
        })

        viewModel.navigateToDetailFrag.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    SolaroidGalleryFragmentDirections.actionGalleryFragmentToDetailFragment(it)
                )
                viewModel.doneNaviToDetailFrag()
            }
        })


        return binding.root

    }
}