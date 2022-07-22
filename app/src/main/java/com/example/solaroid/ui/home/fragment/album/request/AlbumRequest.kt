package com.example.solaroid.ui.home.fragment.album.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumRequestBinding
import com.example.solaroid.dialog.RequestAlbumAcceptDialogFragment
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.models.domain.asFirebaseModel
import com.example.solaroid.room.SolaroidDatabase
import com.example.solaroid.ui.album.adapter.AlbumListAdapter
import com.example.solaroid.ui.home.adapter.AlbumListClickListener

class AlbumRequest : Fragment(), RequestAlbumAcceptDialogFragment.RequestAlbumAcceptDialogListener {
    private lateinit var binding: FragmentAlbumRequestBinding

    private lateinit var viewModel: AlbumRequestViewModel
    private lateinit var viewModelFactory: AlbumRequestViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_album_request, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao
        viewModelFactory = AlbumRequestViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[AlbumRequestViewModel::class.java]

        val onRequestAlbumListener: (album: RequestAlbum) -> Unit = { album ->
            viewModel.setRequestAlbum(album)
        }

        val adapter = AlbumListAdapter(AlbumListClickListener(null, onRequestAlbumListener))

        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let{
                viewModel.refreshRequestAlbums(it.friendCode.drop(1))
            }
        }

        viewModel.requestAlbums.observe(viewLifecycleOwner) {
            it?.let{
                adapter.submitList(it)
            }
        }

        viewModel.requestAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{ album ->
                showDialog(album)
            }
        }

        return binding.root
    }

    fun showDialog(album:RequestAlbum) {
        val new = RequestAlbumAcceptDialogFragment(this, album, "공유 사진첩 요청을 수락하시겠습니까?", "수락", "거절")
        new.show(parentFragmentManager, "RequestAlbumAccept")
    }
    //RequestAlbumAcceptDialog
    override fun onDialogPositiveClick(requestAlbum: RequestAlbum, dialog: DialogFragment) {
        viewModel.setValueInWithAlbum(requestAlbum)
        viewModel.setValueInAlbum(requestAlbum.asFirebaseModel())
        viewModel.deleteRequestAlbumInFirebase(requestAlbum)
    }

    override fun onDialogNegativeClick(requestAlbum: RequestAlbum, dialog: DialogFragment) {
        viewModel.deleteRequestAlbumInFirebase(requestAlbum)
        dialog.dismiss()
    }

}