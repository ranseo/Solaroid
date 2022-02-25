package com.example.solaroid.solaroidframe


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding

class SolaroidFrameFragmentContainer : Fragment(), PopupMenu.OnMenuItemClickListener{

    companion object {
        const val TAG_L = "LATELY"
        const val TAG_F = "FAVORITE"

    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel : SolaroidFrameViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameContainerBinding>(inflater, R.layout.fragment_solaroid_frame_container, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.navigateToLately(true)


        viewModel.popUpMenu.observe(viewLifecycleOwner, Observer {
            if (it) {
                popupShow(binding.popupMenuFilter)
                viewModel.doneFilterPopupMenu()
            }
        })

        viewModel.naviToLately.observe(viewLifecycleOwner, Observer {
           if(it) {
               val lately = childFragmentManager.findFragmentByTag(TAG_L)
               if(lately==null) {
                   childFragmentManager.commitNow {
                       add<SolaroidFrameLately>(R.id.fragment_frame_container_view, TAG_L)
                   }

                   childFragmentManager.commit {
                       replace<SolaroidFrameLately>(R.id.fragment_frame_container_view)
                   }
               }
               viewModel.doneNavigateToLately()
           }
        })

        viewModel.naviToFavorite.observe(viewLifecycleOwner, Observer {
            if(it) {
                val favorite = childFragmentManager.findFragmentByTag(TAG_F)
                if(favorite==null) {
                    childFragmentManager.commitNow {
                        add<SolaroidFrameFavorite>(R.id.fragment_frame_container_view, TAG_F)
                    }

                    childFragmentManager.commit {
                        replace<SolaroidFrameFavorite>(R.id.fragment_frame_container_view)
                    }
                }
                viewModel.doneNavigateToFavorite()
            }
        })

        viewModel.naviToDetailFrag.observe(viewLifecycleOwner,Observer{
           it?.let{
               findNavController().navigate(
                   SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToDetailFragment(it)
               )
               viewModel.doneNavigateToDetail()
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