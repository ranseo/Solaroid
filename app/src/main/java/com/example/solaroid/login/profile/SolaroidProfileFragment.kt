package com.example.solaroid.login.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidProfileBinding

class SolaroidProfileFragment() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidProfileBinding>(inflater, R.layout.fragment_solaroid_profile, container, false)



        return binding.root
    }
}