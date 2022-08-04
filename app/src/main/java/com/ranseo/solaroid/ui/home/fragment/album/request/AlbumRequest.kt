package com.ranseo.solaroid.ui.home.fragment.album.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentAlbumRequestBinding
import com.ranseo.solaroid.dialog.RequestAlbumAcceptDialogFragment
import com.ranseo.solaroid.models.domain.RequestAlbum
import com.ranseo.solaroid.models.domain.asFirebaseModel
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.ui.album.adapter.AlbumListAdapter
import com.ranseo.solaroid.ui.album.adapter.AlbumListDataItem
import com.ranseo.solaroid.ui.home.adapter.AlbumListClickListener

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

        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao
        viewModelFactory = AlbumRequestViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[AlbumRequestViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner

        val onRequestAlbumListener: (album: RequestAlbum) -> Unit = { album ->
            viewModel.setRequestAlbum(album)
        }

        val adapter = AlbumListAdapter(AlbumListClickListener(onRequestAlbumClickListener = onRequestAlbumListener))


        binding.recAlbumRequest.adapter = adapter

        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let{
                viewModel.refreshRequestAlbums(it.friendCode.drop(1))
            }
        }

        viewModel.requestAlbums.observe(viewLifecycleOwner) {
            if(it.isNullOrEmpty()) {
                adapter.submitList(listOf(AlbumListDataItem.RequestAlbumEmpty))
            } else {
                adapter.submitList(it)
            }
        }

        viewModel.requestAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{ album ->
                showDialog(album)
            }
        }


        val manager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        manager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val list = adapter.currentList
                return if(!list.isNullOrEmpty()) {
                    when(adapter.currentList[0]) {
                        is AlbumListDataItem.RequestAlbumEmpty -> {
                            3
                        }
                        is AlbumListDataItem.RequestAlbumDataItem-> {
                            1
                        }
                        else -> 1
                    }
                } else 1

            }
        }

        binding.recAlbumRequest.layoutManager = manager
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