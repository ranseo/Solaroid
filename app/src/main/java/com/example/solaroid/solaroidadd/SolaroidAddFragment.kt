package com.example.solaroid.solaroidadd

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidAddBinding


/**
 * mediaCollection을 열어 사진을 골라 새로운 포토티켓을 만드는 프래그먼트.
 * */
class SolaroidAddFragment : Fragment() {

    private lateinit var viewModel: SolaroidAddViewModel
    private lateinit var viewModelFactory : SolaroidAddViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding  = DataBindingUtil.inflate<FragmentSolaroidAddBinding>(inflater, R.layout.fragment_solaroid_add, container, false)


        return binding.root
    }
}