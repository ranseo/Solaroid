package com.example.solaroid.ui.home.fragment.album.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.custom.view.AlbumThumbnailView
import com.example.solaroid.custom.view.getBitmapFromView
import com.example.solaroid.databinding.FragmentAlbumCreateFinalBinding
import com.example.solaroid.getAlbumIdWithFriendCodes
import com.example.solaroid.getAlbumNameWithFriendsNickname
import com.example.solaroid.joinProfileImgListToString
import com.example.solaroid.models.domain.Friend
import com.example.solaroid.room.SolaroidDatabase

class AlbumCreateFinal() : Fragment() {

    private lateinit var binding: FragmentAlbumCreateFinalBinding

    private lateinit var viewModel: AlbumCreateViewModel
    private lateinit var viewModelFactory: AlbumCreateViewModelFactory

    private lateinit var thumbnail: Bitmap
    private lateinit var albumId: String
    private lateinit var albumName: String

    private val TAG = "AlbumCreateFinal"


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_album_create_final,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = AlbumCreateViewModelFactory(dataSource)
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[AlbumCreateViewModel::class.java]


        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.albumThumbnail.addOnLayoutChangeListener { p0, _, _, _, _, _, _, _, _ ->
            if (p0 != null) {
                val thumbnailBitmap = (p0 as AlbumThumbnailView).getBitmapFromView()
                Log.i(TAG,"addOnLayoutChangeListener : ${thumbnailBitmap}")
                viewModel.setThumbnail(thumbnailBitmap)
            }
        }


        viewModel.naviToAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    AlbumCreateFinalDirections.actinFinalToCreate()
                )
            }
        }

        viewModel.participants.observe(viewLifecycleOwner) {
            it?.let {
                Log.i(TAG, "participants observe : ${it}")
            }
        }

        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let {
                Log.i(TAG, "myProfile observe : ${it.nickname}")
            }
        }



        viewModel.createBitmap.observe(viewLifecycleOwner) {
            it?.let {
                Log.i(TAG, "createBitmap ${it}")
                //binding.albumThumbnail.thumbnailString = it
            }
        }
        viewModel.createId.observe(viewLifecycleOwner) {
            it?.let {
                Log.i(TAG, "createId ${it}")
            }
        }
        viewModel.createName.observe(viewLifecycleOwner) {
            it?.let {
                Log.i(TAG, "createName ${it}")
            }
        }

        viewModel.createParticipants.observe(viewLifecycleOwner) {
            it?.let{
                Log.i(TAG, "createParticipants ${it}")
            }
        }

        viewModel.createSize.observe(viewLifecycleOwner) {
            it?.let{
                Log.i(TAG, "createSize ${it}")
            }
        }

        viewModel.albumKey.observe(viewLifecycleOwner) {
            it?.let{
                viewModel.createRequestAlbum(it)
            }
        }

        binding.btnAccept.setOnClickListener {
            val thumbnailBitmap = (binding.albumThumbnail).getBitmapFromView()
            Log.i(TAG,"binding.btnAccept : ${thumbnailBitmap}")
            viewModel.setThumbnail(thumbnailBitmap)
            viewModel.createAndNavigate()
        }

        binding.btnCancel.setOnClickListener {
            viewModel.setNullCreateProperty()
            viewModel.navigateToAlbum()
        }

        return binding.root
    }
}