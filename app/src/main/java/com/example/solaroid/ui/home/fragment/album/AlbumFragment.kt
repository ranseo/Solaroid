package com.example.solaroid.ui.home.fragment.album

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.ui.album.adapter.AlbumListAdapter
import com.example.solaroid.ui.album.viewmodel.AlbumViewModel
import com.example.solaroid.databinding.FragmentAlbumBinding
import com.example.solaroid.dialog.AlbumCreateDialog
import com.example.solaroid.dialog.AlbumCreateParticipantsDialog
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.ui.album.viewmodel.AlbumType
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlbumFragment : Fragment(),
    AlbumCreateParticipantsDialog.AlbumCreateParticipantsDialogListener,
    AlbumCreateDialog.AlbumCreateDialogListener {
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

        val adapter = AlbumListAdapter()

        binding.recAlbum.adapter = adapter


        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let { profile ->
                viewModel.refreshAlubm(convertHexStringToLongFormat(profile.friendCode))
            }
        }


        viewModel.albumDataItem.observe(viewLifecycleOwner) {
            when (it) {
                AlbumType.ALL -> {
                    Log.i(TAG,"albumDataItem.observe : ALL")
                    adapter.submitList(viewModel.albums.value, viewModel.requestAlbum.value)
                }
                AlbumType.NORMAL -> {
                    Log.i(TAG,"albumDataItem.observe : NORMAL")
                    adapter.submitList(normal = viewModel.albums.value)
                }
                AlbumType.REQUEST -> {
                    Log.i(TAG,"albumDataItem.observe : REQUEST")
                    adapter.submitList(request = viewModel.requestAlbum.value)
                }
                AlbumType.NONE -> {
                    Log.i(TAG,"albumDataItem.observe : NONE")
                    adapter.submitList()
                }
            }
        }


        viewModel.startCreateAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { list ->
                viewModel.setNullCreateProperty()
                showCreateParticipantsDialog(list)
            }
        }

        viewModel.createReady.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                viewModel.createAlbum()
            }
        }


        setOnItemSelectedListener(binding.albumBottomNavi)
        binding.albumBottomNavi.itemIconTintList = null

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.albumBottomNavi.menu.findItem(R.id.album).isChecked = true
    }

    fun showCreateParticipantsDialog(list: List<FriendListDataItem.DialogProfileDataItem>) {
        val new = AlbumCreateParticipantsDialog(this, list)
        new.show(parentFragmentManager, "AlbumCreateParticipants")
    }

    fun showCreateDialog(list: List<Friend>) {
        val new = AlbumCreateDialog(this, list)
        new.show(parentFragmentManager, "AlbumCreate")
    }

    //CreateParticipantsDialogListener
    override fun onParticipantsDialogPositiveClick(friends: List<Friend>, dialog: DialogFragment) {
        viewModel.addParticipants(friends)
        showCreateDialog(friends)
        dialog.dismiss()
    }

    override fun onParticipantsDialogNegativeClick(dialog: DialogFragment) {
        viewModel.setNullCreateProperty()
        dialog.dismiss()
    }

    //CreateDialogListener
    override fun onCreateDialogPositiveClick(
        albumId: String,
        albumName: String,
        thumbnail: Bitmap,
        dialog: DialogFragment
    ) {
        viewModel.setCreateProperty(albumId, albumName, thumbnail)
        viewModel.setCreateReady()
        dialog.dismiss()
    }

    override fun onCreateDialogNegativeClick(dialog: DialogFragment) {
        viewModel.setNullCreateProperty()
        dialog.dismiss()
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
                    true
                }
                R.id.album -> {
                    true
                }

                R.id.add -> {
                    true

                }
                else -> false
            }
        }
    }

}