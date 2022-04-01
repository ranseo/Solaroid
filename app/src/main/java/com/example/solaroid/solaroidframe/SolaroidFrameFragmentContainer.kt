package com.example.solaroid.solaroidframe


import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

open class SolaroidFrameFragmentContainer : Fragment(), PopupMenu.OnMenuItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val TAG = "FrameContainer"
        const val TAG_L = "LATELY"
        const val TAG_F = "FAVORITE"

    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    private lateinit var binding: FragmentSolaroidFrameContainerBinding

    //firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        binding.frameCotainerToolbar.setupWithNavController(navController, appBarConfiguration)
        setNavigationViewListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSolaroidFrameContainerBinding>(
            inflater,
            R.layout.fragment_solaroid_frame_container,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference





        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        database.child("phototicket").setValue(viewModel.photoTickets.value).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "firebase 실시간 데이터베이스로 데이터 전송.")
            } else {
                Log.i(TAG, "firebase 실시간 데이터베이스로 데이터 전송 실패.", it.exception)

            }
        }

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
                        replace(R.id.fragment_frame_container_view, lat, TAG_L)
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
                        replace(R.id.fragment_frame_container_view, favor, TAG_F)
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
            it?.let {
                val photoTicketId = viewModel.photoTicket.value?.id
                Log.d("프레임컨테이너", "PhotoTicketId : ${it}")
                if (photoTicketId != null) {
                    findNavController().navigate(
                        SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToEditFragment(
                            it
                        )
                    )
                }
                viewModel.doneNavigateToEdit()
            }
        })

        viewModel.naviToAddFrag.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToAddFragment()
                )
                viewModel.doneNavigateToAdd()
            }
        })

        viewModel.naviToGallery.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(
                    SolaroidFrameFragmentContainerDirections.actionFrameFragmentContainerToGalleryFragment()
                )
                viewModel.doneNavigateToGallery()
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
            R.id.login_info -> {
                Log.i("프레임컨테이너", "login_info")
                viewModel.logout()
                true
            }
            else -> true

        }
    }


    private fun refreshLoginInfo() {
        val menuItem = binding.navView.menu.findItem(R.id.login_info)
        val user = auth.currentUser!!
        val view = menuItem.actionView

    }

    private fun setNavigationViewListener() {
        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_info -> {
                Log.i("프레임컨테이너", "login_info")
                viewModel.logout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


}