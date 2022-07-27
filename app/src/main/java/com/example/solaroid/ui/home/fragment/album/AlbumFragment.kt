package com.example.solaroid.ui.home.fragment.album

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumBinding
import com.example.solaroid.dialog.ListSetDialogFragment
import com.example.solaroid.dialog.RequestAlbumAcceptDialogFragment
import com.example.solaroid.models.domain.Album
import com.example.solaroid.parseAlbumIdDomainToFirebase
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.ui.album.adapter.AlbumListAdapter
import com.example.solaroid.ui.album.viewmodel.AlbumTag
import com.example.solaroid.ui.album.viewmodel.AlbumViewModel
import com.example.solaroid.ui.album.viewmodel.ClickTag
import com.example.solaroid.ui.home.adapter.AlbumListClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlbumFragment : Fragment(), ListSetDialogFragment.ListSetDialogListener {
    private val TAG = "AlbumFragment"

    private lateinit var binding: FragmentAlbumBinding

    private lateinit var viewModel: AlbumViewModel
    private lateinit var viewModelFactory: AlbumViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album, container, false)

        val application = requireNotNull(activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = AlbumViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[AlbumViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val onAlbumClickListener: (album: Album) -> Unit = { album ->
            viewModel.setAlbum(AlbumTag(album, ClickTag.CLICK))
        }

        val onAlbumLongClickListener: (album: Album) -> Unit = { album ->
            viewModel.setAlbum(AlbumTag(album, ClickTag.LONG))
        }


        val adapter = AlbumListAdapter(
            AlbumListClickListener(
                onAlbumClickListener = onAlbumClickListener,
                onAlbumLongClickListener = onAlbumLongClickListener
            )
        )

        binding.recAlbum.adapter = adapter


        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let { _ ->
                Log.i(TAG, "myProfile : refreshAlbum 실행")
                viewModel.refreshAlbum()
            }
        }

        viewModel.albums.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it)
            }
        }

        viewModel.album.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { albumTag ->
                viewModel.getRoomDatabaseAlbum(albumTag.first.id, albumTag.second)
            }
        }

        viewModel.roomAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { albumTag ->
                val album = albumTag.first
                when (albumTag.second) {
                    ClickTag.CLICK-> findNavController().navigate(
                        AlbumFragmentDirections.actionAlbumToGallery(album.id, album.name)
                    )
                    ClickTag.LONG -> showLongClickDialog()
                }
            }
        }


        navigateToOtherFragment()
        setOnItemSelectedListener(binding.albumBottomNavi)
        binding.albumBottomNavi.itemIconTintList = null

        return binding.root
    }


    /**
     * AlbumFragment에서 다른 프래그먼트로 이동할 수 있도록
     * Navigate LiveData를 관찰하는 메서드.
     * */
    private fun navigateToOtherFragment() {
        viewModel.naviToHome.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToHomeGallery()
                )
            }
        }

        viewModel.naviToCreate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToAblumCreate()
                )
            }
        }

        viewModel.naviToPhotoCreate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToPhotoCreate()
                )
            }
        }

        viewModel.naviToRequest.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToAlbumRequest()
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setTrueAlbumIconInBottomNavi()
    }

    private fun setTrueAlbumIconInBottomNavi() {
        binding.albumBottomNavi.menu.findItem(R.id.album).isChecked = true
    }


    override fun onStop() {
        super.onStop()
        viewModel.removeListener()
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.removeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    private fun setOnItemSelectedListener(
        botNavi: BottomNavigationView
    ) {
        botNavi.setOnItemSelectedListener { it ->
            when (it.itemId) {
                R.id.home -> {
                    viewModel.navigateToHome()
                    true
                }
                R.id.album -> {
                    true
                }
                R.id.create -> {
                    viewModel.navigateToCreate()
                    true
                }
                R.id.request -> {
                    viewModel.navigateToRequest()
                    true
                }

                else -> false
            }
        }
    }

    override fun onDialogListItem(dialog: DialogFragment, position: Int) {
        val databaseAlbum = viewModel.roomAlbum.value?.peekContent()?.first ?: return
        when (position) {
            0 -> {
                viewModel.deleteCurrAlbum(databaseAlbum)
            }
            1 -> {

            }
            else -> {

            }
        }
    }

    fun showLongClickDialog() {
        val new = ListSetDialogFragment(R.array.album_long_click_dialog_items, this)
        new.show(parentFragmentManager, "AlbumLongClick")
    }


}