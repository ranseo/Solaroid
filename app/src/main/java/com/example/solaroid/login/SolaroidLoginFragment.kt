package com.example.solaroid.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.MainActivity
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase

class SolaroidLoginFragment : Fragment() {

//    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
//        FirebaseAuthUIActivityResultContract(), this::onSignResult
//    )

    private lateinit var viewModel: SolaroidLoginViewModel

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidLoginBinding>(
            inflater,
            R.layout.fragment_solaroid_login,
            container,
            false
        )


        viewModel = ViewModelProvider(requireActivity())[SolaroidLoginViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    navigateToMainActivity()
                    requireActivity().finish()
                }
                SolaroidLoginViewModel.AuthenticationState.UNAUTHENTICATED -> {
//                    Toast.makeText(requireActivity(), LoginFail, Toast.LENGTH_LONG).show()
                }
                SolaroidLoginViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    Toast.makeText(requireActivity(), VERIFY, Toast.LENGTH_LONG).show()
                    sendEmailVerifyAccount(auth.currentUser!!)
                }
                else ->{}
            }
        })

        viewModel.loginBtn.observe(viewLifecycleOwner, Observer { login ->
            if (login) {
                val email = binding.etId.text.toString()
                val password = binding.etPassword.text.toString()
                if (email.isEmpty() || password.isEmpty()) return@Observer
                logInWithEmailAndPassword(email, password)
                viewModel.doneLogin()
            }
        })

        viewModel.signUpBtn.observe(viewLifecycleOwner, Observer { signup ->
            if (signup) {
                val email = binding.etId.text.toString()
                val password = binding.etPassword.text.toString()
                if (email.isEmpty() || password.isEmpty()) return@Observer
                createUserWithEmailAndPassword(email, password)
                viewModel.doneSignUp()
            }
        })

//       launchSignInFlow()
        return binding.root
    }




//    private fun onSignResult(result: FirebaseAuthUIAuthenticationResult) {
//        if (result.resultCode == RESULT_OK) {
//            Log.i(TAG, "Fragment OnSignResult Success")
//        } else {
//            Log.d(TAG, "Fragment OnSignResult Failure ${result.idpResponse?.error?.errorCode}}")
//
//        }
//    }


//    private fun launchSignInFlow() {
//        val providers = arrayListOf(
//            AuthUI.IdpConfig.EmailBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build()
//        )
//
//        val signInIntent = AuthUI.getInstance()
//            .createSignInIntentBuilder()
//            .setAvailableProviders(providers)
//            .build()
//
//        signInLauncher.launch(signInIntent)
//    }

    /**
     * 기존 사용자 로그인
     * */
    private fun logInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "로그인 성공.")
                    Toast.makeText(requireContext(), LOGIN_SUCCESS, Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "로그인 실패")
                    Toast.makeText(requireContext(), LOGIN_FAIL, Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * 신규 사용자 가입
     * */
    private fun createUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "회원가입 성공")
                    Toast.makeText(requireContext(), SEND, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.w(TAG, "회원가입 실패", task.exception)
                    Toast.makeText(requireContext(), SIGNUP_FAIL, Toast.LENGTH_SHORT).show()
                }
            }


    }

    private fun sendEmailVerifyAccount(user: FirebaseUser) {
        val url = "https://ssolaroid.page.link/rniX?mode=verifyEmail&uid="+user.uid
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
            .addOnCompleteListener { task->
                if(task.isSuccessful) {
                    Log.i(TAG,"이메일 인증 메시지 전송")
                } else {
                    Log.w(TAG, "이메일 인증 메시지 전송 실패")
                }
            }

    }

    /**
     * 로그인 성공 시 , 메인액티비티로 이동.
     * */
    private fun navigateToMainActivity() {
        startActivity(Intent(requireActivity(), MainActivity::class.java))
        requireActivity().finish()
    }

    /**
     * 로그아웃 시행.
     * */
    fun logout() {
        auth.signOut()
    }


    companion object {
        const val TAG = "Login"
        const val LOGIN_SUCCESS = "로그인 성공"
        const val LOGIN_FAIL = "이메일 또는 비밀번호를 다시 확인해주세요."
        const val SIGNUP_FAIL = "회원가입에 실패하셨습니다."
        const val VERIFY = "해당 이메일은 인증되지 않았습니다."
        const val SEND = "이메일로 인증 메일을 전송하였습니다."
    }
}