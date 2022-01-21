package com.example.solaroid.solaroidcreate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidPhotoCreateBinding


class SolaroidPhotoCreateFragment : Fragment() {

    private lateinit var viewModelFactory : SolaroidPhotoCreateViewModelFactory
    private lateinit var viewModel : SolaroidPhotoCreateViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidPhotoCreateBinding>(inflater, R.layout.fragment_solaroid_photo_create, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = SolaroidPhotoCreateViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidPhotoCreateViewModel::class.java]

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let{
                findNavController().navigate(
                    SolaroidPhotoCreateFragmentDirections.actionCreateFragmentToGalleryFragment()
                )
                viewModel.doneNavigateToGalleryFragment()
            }
        })

        return binding.root
    }
}