package com.example.solaroid.login.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidProfileBinding
import com.example.solaroid.domain.asDatabaseModel
import com.example.solaroid.domain.asFirebaseModel
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.room.SolaroidDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class SolaroidProfileFragment() : Fragment() {

    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var profileObserver: ProfileObserver

    private lateinit var viewModelFactory : SolaroidProfileViewModelFactory
    private lateinit var viewModel: SolaroidProfileViewModel

    private lateinit var auth: FirebaseAuth

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
                    viewModel.insertRefreshUpdateInsert()
                }
                else -> {}
            }
        })

        viewModel.naviToLogin.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let{
                findNavController().navigate(
                    SolaroidProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                )
            }
        }

        viewModel.myProfile.observe(viewLifecycleOwner) {
            it?.let { profile->
                viewModel.insertUserListAndNavigateHome(profile.asDatabaseModel())
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