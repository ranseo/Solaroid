package com.example.solaroid.solaroidframe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameBinding

class SolaroidFrameFragment : Fragment() {

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel : SolaroidFrameViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameBinding>(inflater, R.layout.fragment_solaroid_frame, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this,viewModelFactory)[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = SolaroidFrameAdapter(OnClickListener { photoTicketKey ->
            viewModel.naviToDetail(photoTicketKey)
        })

        viewModel.navigateToDetailFrag.observe(viewLifecycleOwner, Observer{
            it?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameFragmentToDetailFragment(it)
                )
                viewModel.doneNaviToDetailFrag()
            }
        })

        binding.viewpager.adapter = adapter



        return binding.root
    }
}