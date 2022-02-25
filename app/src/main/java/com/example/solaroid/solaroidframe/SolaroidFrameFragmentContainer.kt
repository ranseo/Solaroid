package com.example.solaroid.solaroidframe


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameBinding

class SolaroidFrameFragmentContainer : Fragment(), PopupMenu.OnMenuItemClickListener{

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel : SolaroidFrameViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameBinding>(inflater, R.layout.fragment_solaroid_frame, container, false)

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
           if(it) {
               viewModel.doneNavigateToLately()
           }
        })

        viewModel.naviToFavorite.observe(viewLifecycleOwner, Observer {
            if(it) {
                viewModel.doneNavigateToFavorite()
            }
        })

        viewModel.naviToDetailFrag.observe(viewLifecycleOwner,Observer{
           it?.let{
               findNavController().navigate(
                   SolaroidFrameFragmentContainerDirections.actionFrameContainerToDetailFragment(it)
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