package com.example.solaroid.ui.home.fragment.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.ui.album.adapter.AlbumListAdapter
import com.example.solaroid.ui.album.viewmodel.AlbumViewModel
import com.example.solaroid.databinding.FragmentAlbumBinding
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.ui.album.viewmodel.AlbumType

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

        val application = requireNotNull(activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = AlbumViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[AlbumViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = AlbumListAdapter()

        binding.recAlbum.adapter = adapter


        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let{ profile ->
                viewModel.refreshAlubm(convertHexStringToLongFormat(profile.friendCode))
            }
        }


        viewModel.albumDataItem.observe(viewLifecycleOwner) {
            when(it) {
                AlbumType.ALL -> {
                    adapter.submitList(viewModel.albums.value, viewModel.requestAlbum.value)
                }
                AlbumType.NORMAL -> {
                    adapter.submitList(normal = viewModel.albums.value)
                }
                AlbumType.REQUEST -> {
                    adapter.submitList(request = viewModel.requestAlbum.value)
                }
                AlbumType.NONE -> {
                    adapter.submitList()
                }
            }
        }


        return binding.root
    }
}