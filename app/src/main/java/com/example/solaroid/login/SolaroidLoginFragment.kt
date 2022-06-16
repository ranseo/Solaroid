package com.example.solaroid.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentSolaroidLoginBinding
import com.example.solaroid.firebase.FirebaseManager
import com.example.solaroid.login.signup.SolaroidSignUpFragment
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SolaroidLoginFragment : Fragment() {

//    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
//        FirebaseAuthUIActivityResultContract(), this::onSignResult
//    )

    private lateinit var binding:FragmentSolaroidLoginBinding

    private lateinit var viewModelFactory: LoginViewModelFactory
    private lateinit var viewModel: SolaroidLoginViewModel

    private lateinit var auth: FirebaseAuth


    /*
     * 해당 sharedPreferences는 "아이디 저장" 에 해당하는 변수이다.
     * Str은 저장할 아이디를, Bool은 아이디 저장 여부를.
     * */
    private lateinit var sharedPrefStr: SharedPreferences
    private lateinit var sharedPrefBool: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseManager.getAuthInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSaveIdListener()

        viewModel.setLoginErrorType(SolaroidLoginViewModel.LoginErrorType.EMPTY)

        //아이디 저장
        sharedPrefBool = this.context?.getSharedPreferences(
            getString(R.string.com_example_solaroid_LoginSaveKeyBool),
            Context.MODE_PRIVATE
        ) ?: return

        sharedPrefStr = this.context?.getSharedPreferences(
            getString(R.string.com_example_solaroid_LoginSaveKeyStr),
            Context.MODE_PRIVATE
        ) ?: return


        val isLoginSave =
            sharedPrefBool.getBoolean(getString(R.string.com_example_solaroid_LoginSaveKeyBool), false)

        val savedId = sharedPrefStr.getString(getString(R.string.com_example_solaroid_LoginSaveKeyStr), null)
        if (isLoginSave) {
            viewModel.setSavedLoginId(savedId)
            binding.cbSaveId.isChecked = true
        } else {
            viewModel.setSavedLoginId(savedId)
            binding.cbSaveId.isChecked = false
        }
        ////




    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSolaroidLoginBinding>(
            inflater,
            R.layout.fragment_solaroid_login,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application).photoTicketDao

        viewModelFactory = LoginViewModelFactory(dataSource)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[SolaroidLoginViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.i(TAG, "로그인 성공 userId : ${auth.currentUser?.uid}")
                    if(auth.currentUser == null) auth.signOut()
                    else {
                        Log.i(TAG, "프로필설정 또는 메인컨텐츠 이동")
                        viewModel.setSavedLoginId(auth.currentUser?.email)
                        viewModel.isProfileSet()
                    }
                }
                SolaroidLoginViewModel.AuthenticationState.UNAUTHENTICATED -> {
//                    Toast.makeText(requireActivity(), LoginFail, Toast.LENGTH_LONG).show()
                }
                SolaroidLoginViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    Log.i(TAG, "로그인 인증 확인 안됨")
                    viewModel.setLoginErrorType(SolaroidLoginViewModel.LoginErrorType.INVALID)
                    auth.signOut()
                }
                else -> {}
            }
        })

        viewModel.loginBtn.observe(viewLifecycleOwner, Observer { login ->
            login.getContentIfNotHandled()?.let {
                val email = binding.etId.text.toString()
                val password = binding.etPassword.text.toString()
                checkLoginError(email, password)
            }
        })

        viewModel.loginErrorType.observe(viewLifecycleOwner, Observer { type ->
            when (type) {
                SolaroidLoginViewModel.LoginErrorType.ISRIGHT -> logInWithEmailAndPassword(
                    binding.etId.text.toString(),
                    binding.etPassword.text.toString()
                )
                SolaroidLoginViewModel.LoginErrorType.INVALID -> showSnackbar(
                    binding.coordinatorLayout,
                    SEND_CHECK
                )
                else -> {}
            }
        })

        viewModel.naviToSignUp.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                Log.i(TAG, "viewModel.naviToSignUp")
                findNavController().navigate(
                    SolaroidLoginFragmentDirections.actionLoginFragmentToSignupFragment()
                )

            }
        })

        viewModel.naviToNext.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { isProfileSet ->
                if (isProfileSet) {
                    Log.i(TAG, "naviToNext.observe : 메인 액티비티로 이동")
                    navigateToMainActivity()
                } else {
                    Log.i(TAG, "naviToNext.observe : 프로필 프래그먼트로 이동")
                    findNavController().navigate(
                        SolaroidLoginFragmentDirections.actionLoginFragmentToProfileFragment()
                    )
                }
            }
        }

        viewModel.isSaveId.observe(viewLifecycleOwner) {
            Log.i(TAG, "viewModel.isSaveId : ${it}")
            with(sharedPrefBool.edit()) {
                putBoolean(getString(R.string.com_example_solaroid_LoginSaveKeyBool), it)
                apply()
            }

            putEmailSharedPref(it, viewModel.SavedLoginId.value)
        }



        return binding.root
    }

    fun showSnackbar(layout: CoordinatorLayout, str: String) {
        val sendEmail = Snackbar.make(layout, str, Snackbar.LENGTH_INDEFINITE)
        sendEmail.setAction("전송", SendValidEmail(FirebaseManager.getAuthInstance().currentUser!!))
        sendEmail.show()
    }

    inner class SendValidEmail(val user: FirebaseUser) : View.OnClickListener {
        override fun onClick(p0: View?) {
            sendEmailVerifyAccount(user)
        }
    }

    fun setSaveIdListener() {
        val checkBox = binding.cbSaveId
        checkBox.setOnCheckedChangeListener { _, b ->
            viewModel.setIsSaveId(b)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        putEmailSharedPref(viewModel.isSaveId.value == true , viewModel.SavedLoginId.value)

    }

    fun putEmailSharedPref(flag:Boolean, email:String?) {
        if(flag) {
            with(sharedPrefStr.edit()){
                putString(getString(R.string.com_example_solaroid_LoginSaveKeyStr), email)
                apply()
            }
            Log.i(TAG,"putEmailSharedPref : putString ${email}")
        } else {
            with(sharedPrefStr.edit()){
                putString(getString(R.string.com_example_solaroid_LoginSaveKeyStr), "")
                apply()
            }
            Log.i(TAG,"putEmailSharedPref : putString null")
        }
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
                } else {
                    Log.d(TAG, "로그인 실패")
                    viewModel.setLoginErrorType(SolaroidLoginViewModel.LoginErrorType.ACCOUNTERROR)
                }
            }
    }


    /**
     * 로그인 성공 시 , 메인액티비티로 이동.
     * */
    private fun navigateToMainActivity() {

        findNavController().navigate(
            SolaroidLoginFragmentDirections.actionLoginFragmentToMainActivity()
        )
        requireActivity().finish()
    }


    fun checkLoginError(email: String, password: String) {
        val type =
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.i(TAG, "email.matches fails")
                SolaroidLoginViewModel.LoginErrorType.EMAILTYPEERROR
            } else if (password.isEmpty() || password.length < 8) SolaroidLoginViewModel.LoginErrorType.PASSWORDERROR
            else SolaroidLoginViewModel.LoginErrorType.ISRIGHT

        Log.i(SolaroidSignUpFragment.TAG, "TYPE : ${type}")
        viewModel.setLoginErrorType(type)
    }

    /**
     * 회원가입 성공 시, 이메일로 인증 메일 전송하는 함수
     * */
    private fun sendEmailVerifyAccount(user: FirebaseUser) {
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
                    Log.i(SolaroidSignUpFragment.TAG, "이메일 인증 메시지 전송")
                    Toast.makeText(this.context, "메일을 전송하였습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(SolaroidSignUpFragment.TAG, "이메일 인증 메시지 전송 실패")
                    Toast.makeText(this.context, "메일을 전송에 실패.네트워크 확인", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        const val TAG = "로그인 프래그먼트"

        const val SEND_CHECK = "해당 이메일로 인증 메일을 보내시겠습니까?"
    }
}