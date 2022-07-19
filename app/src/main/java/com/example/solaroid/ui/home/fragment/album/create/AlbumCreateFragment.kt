package com.example.solaroid.ui.home.fragment.album.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumCreateBinding


class AlbumCreateFragment() : Fragment() {

    private lateinit var binding : FragmentAlbumCreateBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_album_create_final,container,false)


        return binding.root
    }
}