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
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.convertHexStringToLongFormat
import com.example.solaroid.ui.album.adapter.AlbumListAdapter
import com.example.solaroid.ui.album.viewmodel.AlbumViewModel
import com.example.solaroid.databinding.FragmentAlbumBinding
import com.example.solaroid.dialog.AlbumCreateDialog
import com.example.solaroid.dialog.AlbumCreateParticipantsDialog
import com.example.solaroid.dialog.NormalDialogFragment
import com.example.solaroid.dialog.RequestAlbumAcceptDialogFragment
import com.example.solaroid.models.domain.*
import com.example.solaroid.parseAlbumIdDomainToFirebase
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.ui.album.viewmodel.AlbumType
import com.example.solaroid.ui.friend.adapter.FriendListDataItem
import com.example.solaroid.ui.home.adapter.AlbumListClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlbumFragment : Fragment(),
    AlbumCreateParticipantsDialog.AlbumCreateParticipantsDialogListener,
    AlbumCreateDialog.AlbumCreateDialogListener,
    RequestAlbumAcceptDialogFragment.RequestAlbumAcceptDialogListener {
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

        val onAlbumListener: (album: Album) -> Unit = { album ->
            viewModel.setAlbum(album)
        }

        val onRequestAlbumListener: (album: RequestAlbum) -> Unit = { album ->
            viewModel.setRequestAlbum(album)
        }

        val adapter = AlbumListAdapter(
            AlbumListClickListener(
                onAlbumListener,
                onRequestAlbumListener
            )
        )

        binding.recAlbum.adapter = adapter


        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let { profile ->
                viewModel.refreshAlubm(convertHexStringToLongFormat(profile.friendCode))
            }
        }


        viewModel.albumDataItem.observe(viewLifecycleOwner) {
            when (it) {
                AlbumType.ALL -> {
                    Log.i(TAG, "albumDataItem.observe : ALL")
                    adapter.submitList(viewModel.albums.value, viewModel.requestAlbums.value)
                }
                AlbumType.NORMAL -> {
                    Log.i(TAG, "albumDataItem.observe : NORMAL")
                    adapter.submitList(normal = viewModel.albums.value)
                }
                AlbumType.REQUEST -> {
                    Log.i(TAG, "albumDataItem.observe : REQUEST")
                    adapter.submitList(request = viewModel.requestAlbums.value)
                }
                AlbumType.NONE -> {
                    Log.i(TAG, "albumDataItem.observe : NONE")
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

        viewModel.naviToHome.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToHomeGallery()
                )
            }
        }

        viewModel.naviToCreate.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let{
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToCreate()
                )
            }

        }

        viewModel.album.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { album ->
                viewModel.getRoomDatabaseAlbum(album.id)
            }
        }


        viewModel.roomAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { album ->
                findNavController().navigate(
                    AlbumFragmentDirections.actionAlbumToGallery(album.id, album.key)
                )
            }
        }

        viewModel.requestAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { album ->
                showRequestAlbumClickDialog(album)
            }
        }



        setOnItemSelectedListener(binding.albumBottomNavi)
        binding.albumBottomNavi.itemIconTintList = null

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setTrueAlbumIconInBottomNavi()
    }

    private fun setTrueAlbumIconInBottomNavi() {
        binding.albumBottomNavi.menu.findItem(R.id.album).isChecked = true
    }


    //<dialog>
    private fun showRequestAlbumClickDialog(album: RequestAlbum) {
        val new = RequestAlbumAcceptDialogFragment(this, album, "앨범 참여 요청을 받아들이시겠습니까?", "수락", "거절")
        new.show(parentFragmentManager, "RequestAlbumClick")
    }

    private fun showCreateParticipantsDialog(list: List<FriendListDataItem.DialogProfileDataItem>) {
        val new = AlbumCreateParticipantsDialog(this, list, viewModel.myProfile.value!!)
        new.show(parentFragmentManager, "AlbumCreateParticipants")
    }

    private fun showCreateDialog(list: List<Friend>) {
        val new = AlbumCreateDialog(this, list, viewModel.myProfile.value!!.asFriend(""))
        new.show(parentFragmentManager, "AlbumCreate")
    }
    //</dialog>


    //CreateParticipantsDialogListener
    override fun onParticipantsDialogPositiveClick(friends: List<Friend>, dialog: DialogFragment) {
        viewModel.addParticipants(friends)
        showCreateDialog(friends)
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
    }

    override fun onCreateDialogNegativeClick(dialog: DialogFragment) {
        viewModel.setNullCreateProperty()
        dialog.dismiss()
    }


    //RequestAlbumAcceptDialog
    override fun onDialogPositiveClick(requestAlbum: RequestAlbum, dialog: DialogFragment) {
        viewModel.setValueInWithAlbum(requestAlbum)
        viewModel.deleteRequestAlbumInFirebase(requestAlbum)
        viewModel.setValueInAlbum(requestAlbum.asDomainModel())
    }

    override fun onDialogNegativeClick(requestAlbum: RequestAlbum, dialog: DialogFragment) {
        viewModel.deleteRequestAlbumInFirebase(requestAlbum)
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
                    viewModel.navigateToHome()
                    true
                }
                R.id.album -> {
                    true
                }
                R.id.add -> {
                    viewModel.onCreateAlbumBtn()
                    true
                }

                else -> false
            }
        }
    }


}