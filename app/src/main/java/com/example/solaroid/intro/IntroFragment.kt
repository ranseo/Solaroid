package com.example.solaroid.intro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.solaroid.MainActivity
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidIntroBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class IntroFragment : Fragment() {
    private lateinit var binding: FragmentSolaroidIntroBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_solaroid_intro, container, false)
        binding.introImage.playAnimation()

        auth = FirebaseManager.getAuthInstance()

        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG,"CoroutineScope(Dispatchers.IO).launch")
            val user = auth.currentUser
            delay(1000)
            if (user == null || !user.isEmailVerified) {
                startLoginActivity()
            } else startMainActivity()
        }.start()


        return binding.root
    }

    suspend fun startMainActivity() {
        withContext(Dispatchers.Main) {
            findNavController().navigate(
                IntroFragmentDirections.actionIntroFragmentToFrameFragmentContainer()
            )
        }

    }

    suspend fun startLoginActivity() {
        withContext(Dispatchers.Main) {
            findNavController().navigate(
                IntroFragmentDirections.globalActionMainActivityToLoginActivity()
            )
        }
    }

    companion object {
        const val TAG = "인트로프래그먼트"
    }
}