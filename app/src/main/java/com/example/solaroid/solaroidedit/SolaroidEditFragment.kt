package com.example.solaroid.solaroidedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidEditBinding

class SolaroidEditFragment : Fragment() {

    private lateinit var viewModel : SolaroidEditFragmentViewModel
    private lateinit var viewModedlFactory: SolaroidEditFragmentViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidEditBinding>(inflater, R.layout.fragment_solaroid_edit, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        val key = SolaroidEditFragmentArgs.fromBundle(requireArguments()).photoTicketKey

        viewModedlFactory = SolaroidEditFragmentViewModelFactory(key, dataSource.photoTicketDao)
        viewModel = ViewModelProvider(requireActivity(), viewModedlFactory)[SolaroidEditFragmentViewModel::class.java]


        return binding.root
    }
}