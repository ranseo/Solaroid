package com.example.solaroid.ui.home.fragment.album.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumRequestBinding
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.ui.album.adapter.AlbumListAdapter
import com.example.solaroid.ui.home.adapter.AlbumListClickListener

class AlbumRequest : Fragment() {
    private lateinit var binding: FragmentAlbumRequestBinding

    private lateinit var viewModel: AlbumRequestViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_album_request, container, false)

        viewModel = ViewModelProvider(this)[AlbumRequestViewModel::class.java]

        val onRequestAlbumListener: (album: RequestAlbum) -> Unit = { album ->
            viewModel.setRequestAlbum(album)
        }

        val adapter = AlbumListAdapter(AlbumListClickListener(null, onRequestAlbumListener))


        return binding.root
    }
}