package com.example.solaroid.solaroidframe

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView

class SolaroidFrameFragment : Fragment() {

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameBinding>(
            inflater,
            R.layout.fragment_solaroid_frame,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)
        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = SolaroidFrameAdapter(OnClickListener { photoTicketKey ->
            viewModel.naviToDetail(photoTicketKey)
        })

        viewModel.navigateToDetailFrag.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameFragmentToDetailFragment(it)
                )
                viewModel.doneNaviToDetailFrag()
            }
        })

        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("FrameFragment", "viewModel.photoTicket.observe  : ${it.id}")
                viewModel.setCurrentFavorite(it.favorites)
            }

        })

        viewModel.favorite.observe(viewLifecycleOwner, Observer { favor ->
            favor?.let {
                Log.d("FrameFragment", "viewModel.favorite.observe  : ${favor}")
                val menuItem: MenuItem = binding.fragmentFrameBottomNavi.menu.findItem(R.id.favorite)
                menuItem.setIcon(if (!it) R.drawable.ic_favorite_false else R.drawable.ic_favorite_true)
                Log.d("FrameFragment", "Success")
            }
        })

        binding.viewpager.adapter = adapter


        //02.21 얘도정리.
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val photoTicket = adapter.getPhotoTicket(position)
                Log.d("FrameFragment", "onPageSelected, photoTicket id : ${photoTicket.id}")
                viewModel.setCurrentPhotoTicket(photoTicket)
            }
        })


        //02.21 이거정리.
        binding.fragmentFrameBottomNavi.setOnItemSelectedListener {
            if (it.itemId == R.id.favorite) {
                val favorite = viewModel.favorite.value
                if(favorite!=null) {
                    if(favorite) viewModel.togglePhotoTicketFavorite(false) else viewModel.togglePhotoTicketFavorite(true)
                }
            }
            false
        }

        return binding.root
    }
}