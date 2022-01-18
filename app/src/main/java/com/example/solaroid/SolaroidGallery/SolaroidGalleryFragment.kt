package com.example.solaroid.SolaroidGallery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidGalleryBinding

class SolaroidGalleryFragment :Fragment() {

    private lateinit var galleryViewModelFactory: SolaroidGalleryViewModelFactory
    private lateinit var galleryViewModel : SolaroidGalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidGalleryBinding>(inflater,R.layout.fragment_solaroid_gallery, container, false)



        return binding.root

    }
}