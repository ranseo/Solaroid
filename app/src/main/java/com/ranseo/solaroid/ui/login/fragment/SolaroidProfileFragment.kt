package com.ranseo.solaroid.ui.login.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentSolaroidProfileBinding
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.ui.login.ProfileObserver
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidProfileViewModel
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidProfileViewModelFactory
import com.ranseo.solaroid.room.SolaroidDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class SolaroidProfileFragment() : Fragment() {

    private lateinit var profileObserver: ProfileObserver

    private lateinit var viewModelFactory : SolaroidProfileViewModelFactory
    private lateinit var viewModel: SolaroidProfileViewModel

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidProfileBinding>(
            inflater,
            R.layout.fragment_solaroid_profile,
            container,
            false
        )

        auth = FirebaseManager.getAuthInstance()

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = SolaroidProfileViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this,viewModelFactory)[SolaroidProfileViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        profileObserver = ProfileObserver(this.requireActivity().activityResultRegistry, viewModel)
        lifecycle.addObserver(profileObserver)

        viewModel.naviToMain.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                    Log.i(TAG, "메인액티비티 이동")

                    findNavController().navigate(
                        SolaroidProfileFragmentDirections.actionProfileFragmentToMainActivity()
                    )

                binding.progressBar.visibility = View.GONE
                this.requireActivity().finish()
            }
        })

        viewModel.addImage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                profileObserver.selectImage()
            }
        })

        viewModel.setProfile.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.setProfileType(
                    if (viewModel.profileUrl.value.isNullOrEmpty()) SolaroidProfileViewModel.ProfileErrorType.IMAGEERROR
                    else if (viewModel.nickname.value.isNullOrEmpty()) SolaroidProfileViewModel.ProfileErrorType.NICKNAMEERROR
                    else SolaroidProfileViewModel.ProfileErrorType.ISRIGHT
                )
            }
        })

        viewModel.profileType.observe(viewLifecycleOwner, Observer { type ->
            when (type) {
                SolaroidProfileViewModel.ProfileErrorType.ISRIGHT -> {
                    binding.progressBar.visibility = View.VISIBLE
                    viewModel.insertAndUpdateProfile()
                }
                else -> {}
            }
        })

        viewModel.firebaseProfile.observe(viewLifecycleOwner) {
            it?.let{ profile ->
                viewModel.insertAndNavigateMain(profile)
            }
        }

        viewModel.naviToLogin.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let{
                findNavController().navigate(
                    SolaroidProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                )
            }
        }


        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setProfileType(SolaroidProfileViewModel.ProfileErrorType.EMPTY)
    }


    companion object {
        const val TAG = "프로필 프래그먼트"
    }
}