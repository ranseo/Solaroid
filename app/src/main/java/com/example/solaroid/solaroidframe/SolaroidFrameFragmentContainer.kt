package com.example.solaroid.solaroidframe


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

open class SolaroidFrameFragmentContainer : Fragment(), PopupMenu.OnMenuItemClickListener {

    companion object {
        const val TAG_L = "LATELY"
        const val TAG_F = "FAVORITE"

    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameContainerBinding>(
            inflater,
            R.layout.fragment_solaroid_frame_container,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.popUpMenu.observe(viewLifecycleOwner, Observer {
            if (it) {
                popupShow(binding.popupMenuFilter)
                viewModel.doneFilterPopupMenu()
            }
        })

        viewModel.naviToLately.observe(viewLifecycleOwner, Observer {
            if (it) {
                val lately = childFragmentManager.findFragmentByTag(TAG_L)
                if (lately == null) {
//                    childFragmentManager.commitNow {
//                        add<SolaroidFrameLately>(R.id.fragment_frame_container_view, TAG_L)
//                    }

                    childFragmentManager.commit {
                        val lat = SolaroidFrameLately()
                        replace(R.id.fragment_frame_container_view, lat,TAG_L)
                    }
                }
                viewModel.doneNavigateToLately()
            }
        })

        viewModel.naviToFavorite.observe(viewLifecycleOwner, Observer {
            if (it) {
                val favorite = childFragmentManager.findFragmentByTag(TAG_F)
                if (favorite == null) {
//                    childFragmentManager.commitNow {
//                        add<SolaroidFrameFavorite>(R.id.fragment_frame_container_view, TAG_F)
//                    }

                    childFragmentManager.commit {
                        val favor = SolaroidFrameFavorite()
                        replace(R.id.fragment_frame_container_view,favor, TAG_F)
                    }
                }
                viewModel.doneNavigateToFavorite()
            }
        })

        viewModel.naviToDetailFrag.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToDetailFragment(
                        it
                    )
                )
                viewModel.doneNavigateToDetail()
            }
        })


        viewModel.naviToCreateFrag.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToCreateFragment()
                )
                viewModel.doneNavigateToCreate()
            }
        })


        viewModel.naviToEditFrag.observe(viewLifecycleOwner, Observer {
            it?.let{
                val photoTicketId = viewModel.photoTicket.value?.id
                Log.d("프레임컨테이너" , "PhotoTicketId : ${it}")
                if (photoTicketId != null) {
                    findNavController().navigate(
                        SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToEditFragment(it)
                    )
                }
                viewModel.doneNavigateToEdit()
            }
        })

        viewModel.naviToAddFrag.observe(viewLifecycleOwner, Observer{
            if(it){
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToAddFragment()
                )
                viewModel.doneNavigateToAdd()
            }
        })




        return binding.root
    }

    private fun popupShow(view: View) {
        val popUp = PopupMenu(this.activity, view)
        popUp.setOnMenuItemClickListener(this@SolaroidFrameFragmentContainer)
        popUp.menuInflater.inflate(R.menu.fragment_frame_popup_menu, popUp.menu)
        popUp.show()
    }


    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        return when (p0?.itemId) {
            R.id.filter_lately -> {
                viewModel.sortByFilter(PhotoTicketFilter.LATELY)
                //Toast.makeText(this.activity, "즐겨찾기", Toast.LENGTH_SHORT).show()
                viewModel.navigateToLately(true)
                true
            }
            R.id.filter_favorite -> {
                viewModel.sortByFilter(PhotoTicketFilter.FAVORITE)
                //Toast.makeText(this.activity, "즐겨찾기", Toast.LENGTH_SHORT).show()
                viewModel.navigateToFavorite(true)
                true
            }
            else -> true

        }
    }


}