package com.example.solaroid.album.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.album.adapter.AlbumListAdapter
import com.example.solaroid.album.viewmodel.AlbumViewModel
import com.example.solaroid.album.viewmodel.AlbumViewModelFactory
import com.example.solaroid.databinding.FragmentAlbumBinding

class AlbumFragment :Fragment() {

    private lateinit var binding:FragmentAlbumBinding

    private lateinit var viewModel: AlbumViewModel
    private lateinit var viewModelFactory: AlbumViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_album,container,false)

        viewModelFactory = AlbumViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[AlbumViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = AlbumListAdapter()

        binding.recAlbum.adapter = adapter


        return binding.root
    }
}