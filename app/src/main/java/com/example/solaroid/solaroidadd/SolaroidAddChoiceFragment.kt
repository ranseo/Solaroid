package com.example.solaroid.solaroidadd

import SolaroidAddViewModelFactory
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.adapter.SolaroidChoiceAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidAddChoiceBinding

class SolaroidAddChoiceFragment : Fragment() {

    private lateinit var viewModel: SolaroidAddViewModel
    private lateinit var viewModelFactory: SolaroidAddViewModelFactory

    private lateinit var backPressCallback :OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidAddChoiceBinding>(
            inflater,
            R.layout.fragment_solaroid_add_choice,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)
        viewModelFactory = SolaroidAddViewModelFactory(dataSource.photoTicketDao)
        viewModel = ViewModelProvider(
            requireParentFragment(),
            viewModelFactory
        )[SolaroidAddViewModel::class.java]

        val adapter = SolaroidChoiceAdapter()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.adapter = adapter

        viewModel.images.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressedInChoice()
                return
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,backPressCallback)
    }

    override fun onDetach() {
        super.onDetach()
        backPressCallback.remove()
    }
}