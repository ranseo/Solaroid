package com.example.solaroid.ui.login.fragment

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidSignupBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.ui.login.viewmodel.SolaroidSignUpViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseUser

class SolaroidSignUpFragment : Fragment() {
    private lateinit var viewModel: SolaroidSignUpViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setSignUpErrorType(SolaroidSignUpViewModel.SignUpErrorType.EMPTY)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidSignupBinding>(
            inflater,
            R.layout.fragment_solaroid_signup,
            container,
            false
        )

        viewModel = ViewModelProvider(this)[SolaroidSignUpViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.signUpBtn.observe(viewLifecycleOwner, Observer { signup ->
            signup.getContentIfNotHandled()?.let {
                val email = binding.etEmailId.text.toString()
                val password = binding.etPassword.text.toString()
                checkSignUpError(email, password)
            }
        })

        viewModel.signUpErrorType.observe(viewLifecycleOwner, Observer { type ->
            val emptyType = SolaroidSignUpViewModel.SignUpErrorType.EMPTY
            when (type) {
                SolaroidSignUpViewModel.SignUpErrorType.ISRIGHT -> {
                    Log.i(TAG, "createUserWithEmail")
                    Log.i(TAG, "ID : ${binding.etEmailId.text.toString()}, PASSWORD : ${binding.etPassword.text.toString()}")
                    createUserWithEmailAndPassword(
                        binding.coordinatorLayoutForSignup,
                        binding.etEmailId.text.toString(),
                        binding.etPassword.text.toString()
                    )
                    viewModel.setSignUpErrorType(emptyType)
                }
                else -> {}
            }
        })

        viewModel.naviToLogin.observe(viewLifecycleOwner, Observer{
            it.getContentIfNotHandled()?.let{
                findNavController().navigate(
                    SolaroidSignUpFragmentDirections.actionSignupFragmentToLoginFragment()
                )
            }
        })

        return binding.root
    }

    fun showSnackbar(layout: CoordinatorLayout, str: String) {
        val sendSnackbar = Snackbar.make(layout, str, Snackbar.LENGTH_INDEFINITE)
//        sendSnackbar.setAction("??????", moveEmailListener())
        sendSnackbar.setTextColor(ContextCompat.getColor(requireView().context, R.color.white))
        sendSnackbar.setAction("????????? ??????\n??????", MoveLoginFragment())
        sendSnackbar.show()
    }

    inner class MoveLoginFragment : View.OnClickListener {
        override fun onClick(p0: View?) {
            FirebaseManager.getAuthInstance().signOut()
            viewModel.navigateToLogin()
        }
    }

    fun checkSignUpError(email: String, password: String) {
        val type =
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.i(TAG, "email.matches fails")
                SolaroidSignUpViewModel.SignUpErrorType.EMAILTYPEERROR
            }
            else if (password.isEmpty() || password.length < 8) SolaroidSignUpViewModel.SignUpErrorType.PASSWORDERROR
            else SolaroidSignUpViewModel.SignUpErrorType.ISRIGHT

        Log.i(TAG,"TYPE : ${type}")
        viewModel.setSignUpErrorType(type)
    }

    /**
     * ?????? ????????? ??????
     * */
    private fun createUserWithEmailAndPassword(
        layout: CoordinatorLayout,
        email: String,
        password: String
    ) {
        Log.i(TAG,"createUserWithEmailAndPassword fun")
        FirebaseManager.getAuthInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "???????????? ??????")
                    sendEmailVerifyAccount(layout, FirebaseManager.getAuthInstance().currentUser!!)
                } else {
                    Log.w(TAG, "???????????? ??????", task.exception)
                    showSnackbar(layout, SIGNUP_FAIL)

                }
            }
    }

    /**
     * ???????????? ?????? ???, ???????????? ?????? ?????? ???????????? ??????
     * */
    private fun sendEmailVerifyAccount(layout: CoordinatorLayout, user: FirebaseUser) {
        val url = "https://ssolaroid.page.link/rniX?mode=verifyEmail&uid=" + user.uid
        val actionCodeSetting = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setAndroidPackageName(
                "com.example.solaroid.login",
                true,
                null
            )
            .setHandleCodeInApp(true)
            .build()

        user.sendEmailVerification(actionCodeSetting)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "????????? ?????? ????????? ??????")
                    showSnackbar(layout, SIGNUP_SUCCESS)
                } else {
                    Log.w(TAG, "????????? ?????? ????????? ?????? ??????")

                }
            }
    }

    companion object {
        const val TAG = "???????????? ???????????????"
        const val SIGNUP_FAIL = "??????????????? ?????????????????????.\n?????? ??????????????? ????????? ?????? ?????????."
        const val SIGNUP_SUCCESS = "??????????????? ??????????????????.\n?????? ????????? ?????? ?????? ?????? ????????? ???????????????."
    }
}