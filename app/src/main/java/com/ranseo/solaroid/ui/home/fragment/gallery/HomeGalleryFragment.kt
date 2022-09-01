package com.ranseo.solaroid.ui.home.fragment.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentSolaroidGalleryBinding
import com.ranseo.solaroid.dialog.FilterDialogFragment
import com.ranseo.solaroid.parseAlbumIdDomainToFirebase
import com.ranseo.solaroid.room.SolaroidDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ranseo.solaroid.adapter.*
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.ui.album.adapter.AlbumListDataItem

class HomeGalleryFragment : Fragment(), FilterDialogFragment.OnFilterDialogListener {

    private lateinit var viewModelFactory: HomeGalleryViewModelFactory
    private lateinit var viewModel: HomeGalleryViewModel
    private lateinit var binding: FragmentSolaroidGalleryBinding

    private lateinit var backPressedCallback: OnBackPressedCallback

    private lateinit var filterDialogFragment: FilterDialogFragment


    override fun onAttach(context: Context) {
        super.onAttach(context)
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when(viewModel.photoTicketState.value?.peekContent()) {
                    PhotoTicketState.LONG-> {
                        viewModel.changePhotoTicketState()
                    }
                    else -> {
                        requireActivity().finish()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onDetach() {
        super.onDetach()
        backPressedCallback.remove()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_solaroid_gallery, container, false)
        setHasOptionsMenu(true)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = HomeGalleryViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeGalleryViewModel::class.java]


        val longListenerClick : (photoTicket:PhotoTicket) -> Unit = { photoTicket ->
            viewModel.addOrRemoveDeleteList(photoTicket)
        }

        val longListenerLongClick : () -> Unit = {
            viewModel.changePhotoTicketState()
        }


        val adapter = SolaroidGalleryAdapter(OnGalleryClickListener { photoTicket ->
            viewModel.navigateToFrame(photoTicket)
        }, OnGalleryLongClickListener(longListenerClick, longListenerLongClick), application)

        binding.photoTicketRec.adapter = adapter

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.albums.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToAlbum()
                )
            } else {
                for (album in list) {
                    viewModel.refreshFirebaseListener(
                        parseAlbumIdDomainToFirebase(album.id, album.key),
                        album.key
                    )
                }
            }
        }

        viewModel.photoTicketState.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                Log.i(TAG,"state : ${state}")
                val list = viewModel.photoTickets.value
                if (!list.isNullOrEmpty()) {
                    val dataItemList = when (state) {
                        PhotoTicketState.LONG -> {
                            list.map { GalleryListDataItem.LongClickGalleryDataItem(it) }
                        }
                        else -> {
                            list.map { GalleryListDataItem.NormalGalleryDataItem(it) }
                        }
                    }
                    adapter.submitList(dataItemList)
                } else {
                    adapter.submitList(listOf(GalleryListDataItem.GalleryEmptyDataItem))
                }
            }
        }

        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            if(!list.isNullOrEmpty()) {
                Log.i(TAG, "viewModel.photoTickets.observe(viewLifecycleOwner) { list -> ${list} }")
                val dataItemList = when (viewModel.photoTicketState.value?.peekContent()) {
                    PhotoTicketState.LONG -> {
                        list.map { GalleryListDataItem.LongClickGalleryDataItem(it) }
                    }
                    else -> {
                        list.map { GalleryListDataItem.NormalGalleryDataItem(it) }
                    }
                }
                adapter.submitList(dataItemList)
            } else {
                adapter.submitList(listOf(GalleryListDataItem.GalleryEmptyDataItem))
            }
        }



        navgiateToOtherFragment()

        //binding.galleryBottomNavi.setupWithNavController(findNavController())
        setOnItemSelectedListener(binding.galleryBottomNavi)
        binding.galleryBottomNavi.itemIconTintList = null

        filterDialogFragment = FilterDialogFragment(this)

        val manager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val list = adapter.currentList
                return if (!list.isNullOrEmpty()) {
                    when (adapter.currentList[0]) {
                        is GalleryListDataItem.GalleryEmptyDataItem -> {
                            3
                        }
                        else -> 1
                    }
                } else 1

            }
        }

        binding.photoTicketRec.layoutManager = manager
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshPhtoTicketState()
        viewModel.clearDeleteList()
        binding.galleryBottomNavi.menu.findItem(R.id.home).isChecked = true

    }



    /**
     * gallery Fragment에서 viewModel naviTo 라이브 객체를 관찰하여
     * 원하는 Fragment로 이동하기 위한 코드를 모아놓은 함수
     * */
    private fun navgiateToOtherFragment() {
        viewModel.naviToFrame.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { photoTicket ->
                val filter = viewModel.filter.value?.filter ?: "DESC"
                val albumId = photoTicket.albumInfo[0]
                val albumKey = photoTicket.albumInfo[1]
                Log.i(TAG, "albumId : ${albumId}, albumKey: $albumKey")
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToFrame(
                        filter,
                        photoTicket,
                        albumId,
                        albumKey
                    )
                )
            }
        }

        viewModel.naviToAdd.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToAdd()
                )
            }
        }

        viewModel.naviToCreate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToCreate()
                )
            }
        }

        viewModel.naviToAlbum.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    HomeGalleryFragmentDirections.actionHomeGalleryToAlbum()
                )
            }
        }


    }


    private fun showFilterDialog() {
        filterDialogFragment.show(parentFragmentManager, "filterDialog")
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_gallery_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter -> {
                showFilterDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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
                    viewModel.navigateToAlbum()
                    true
                }

                R.id.add -> {
                    viewModel.navigateToAdd()
                    true

                }
                R.id.remove -> {
                    when(viewModel.photoTicketState.value?.peekContent()) {
                        PhotoTicketState.LONG-> {
                            viewModel.deletePhotoTickets()
                        }
                        else ->{
                            viewModel.changePhotoTicketState()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onFilterDesc() {
        viewModel.setFilter("DESC")
    }

    override fun onFilterAsc() {
        viewModel.setFilter("ASC")
    }

    override fun onFilterFavorite() {
        viewModel.setFilter("FAVORITE")
    }

    override fun onDestroy() {
        viewModel.removeListener()
        super.onDestroy()
    }


    companion object {
        const val TAG = "홈_갤러리_프래그먼트"
    }
}
