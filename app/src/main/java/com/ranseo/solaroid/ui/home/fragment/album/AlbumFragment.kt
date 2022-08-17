package com.ranseo.solaroid.ui.home.fragment.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentAlbumBinding
import com.ranseo.solaroid.dialog.ListSetDialogFragment
import com.ranseo.solaroid.dialog.RenameDialog
import com.ranseo.solaroid.models.domain.Album
import com.ranseo.solaroid.models.room.DatabaseAlbum
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.ui.album.adapter.AlbumListAdapter
import com.ranseo.solaroid.ui.album.adapter.AlbumListDataItem
import com.ranseo.solaroid.ui.album.viewmodel.AlbumTag
import com.ranseo.solaroid.ui.album.viewmodel.AlbumViewModel
import com.ranseo.solaroid.ui.album.viewmodel.ClickTag
import com.ranseo.solaroid.ui.home.adapter.AlbumListClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlbumFragment : Fragment(), ListSetDialogFragment.ListSetDialogListener, RenameDialog.RenameDialogListener {
    private val TAG = "AlbumFragment"

    private lateinit var binding: FragmentAlbumBinding

    private lateinit var viewModel: AlbumViewModel
    private lateinit var viewModelFactory: AlbumViewModelFactory

    private lateinit var adapter : AlbumListAdapter

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


        adapter = AlbumListAdapter(
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
            if(it.isNullOrEmpty()) {
                adapter.submitList(listOf(AlbumListDataItem.NormalAlbumEmpty))
            } else {
                adapter.submitList( it.map { v -> AlbumListDataItem.NormalAlbumDataItem(v) })
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


        val manager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        manager.spanSizeLookup = object:GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val list = adapter.currentList
                return if(!list.isNullOrEmpty()) {
                    when(adapter.currentList[0]) {
                        is AlbumListDataItem.NormalAlbumEmpty -> {
                            3
                        }
                        is AlbumListDataItem.NormalAlbumDataItem -> {
                            1
                        }
                        else -> 1
                    }
                } else 1

            }
        }

        binding.recAlbum.layoutManager = manager
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
        setBadgeOnBottomNavigationView(5,0)
    }

    private fun setTrueAlbumIconInBottomNavi() {
        binding.albumBottomNavi.menu.findItem(R.id.album).isChecked = true
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.removeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setBadgeOnBottomNavigationView(cnt:Int, idx:Int) {
        val bottomNavi = binding.albumBottomNavi.getChildAt(0) as BottomNavigationMenuView
        val itemView = bottomNavi.getChildAt(1) as BottomNavigationItemView


        BadgeDrawable.create(requireContext()).apply{
            number=cnt
            backgroundColor = ContextCompat.getColor(requireContext(), R.color.alert_color)
            badgeTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            horizontalOffset = 33
            verticalOffset = 33
        }.let{ badge ->
            itemView.foreground = badge
            itemView.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->
                BadgeUtils.attachBadgeDrawable(badge, view)
            }
        }
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
                showRenameDialog(databaseAlbum)
            }
            1 -> {

            }
            else -> {
                viewModel.deleteCurrAlbum(databaseAlbum)
            }
        }
    }

    fun showLongClickDialog() {
        val new = ListSetDialogFragment(R.array.album_long_click_dialog_items, this)
        new.show(parentFragmentManager, "AlbumLongClick")
    }

    fun showRenameDialog(album:DatabaseAlbum) {
        val new = RenameDialog(this,"사진첩의 이름을 변경하세요","변경", "취소", album.name)
        new.show(parentFragmentManager, "RenameDialog")
    }

    override fun onRenamePositive(dialog: DialogFragment, new:String) {
        viewModel.editAlbum(new)
    }

    override fun onRenameNegatvie(dialog: DialogFragment) {
        dialog.dismiss()
    }


}