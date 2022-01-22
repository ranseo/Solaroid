package com.example.solaroid.solaroiddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidDetailBinding


class SolaroidDetailFragment : Fragment() {

    private lateinit var viewModel : SolaroidDetailViewModel
    private lateinit var viewModelFactory : SolaroidDetailViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidDetailBinding>(inflater, R.layout.fragment_solaroid_detail,container, false)

        val application = requireNotNull(activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        val photoTicketKey = SolaroidDetailFragmentArgs.fromBundle(requireArguments()).photoTickeyKey

        viewModelFactory = SolaroidDetailViewModelFactory(photoTicketKey, dataSource.photoTicketDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidDetailViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        return binding.root
    }
}