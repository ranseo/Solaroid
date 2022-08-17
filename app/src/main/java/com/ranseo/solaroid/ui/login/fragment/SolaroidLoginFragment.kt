package com.ranseo.solaroid.ui.login.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.ui.login.viewmodel.LoginViewModelFactory
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidLoginViewModel
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.FragmentSolaroidLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolaroidLoginFragment : Fragment() {

//    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
//        FirebaseAuthUIActivityResultContract(), this::onSignResult
//    )

    private lateinit var binding: FragmentSolaroidLoginBinding

    private lateinit var viewModelFactory: LoginViewModelFactory
    private lateinit var viewModel: SolaroidLoginViewModel

    private lateinit var coroutineScope: CoroutineScope

    //로그인
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions
    private var kakaoToken: Boolean = false
    private lateinit var providerBuilder: OAuthProvider.Builder


    /*
    * 해당 sharedPreferences는 "아이디 저장" 에 해당하는 변수이다.
    * Str은 저장할 아이디를, Bool은 아이디 저장 여부를.
    * */
    private lateinit var sharedPrefStr: SharedPreferences
    private lateinit var sharedPrefBool: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseManager.getAuthInstance()
        kakaoToken = AuthApiClient().hasToken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSaveIdListener()

        viewModel.setLoginErrorType(SolaroidLoginViewModel.LoginErrorType.EMPTY)

        //아이디 저장
        sharedPrefBool = this.context?.getSharedPreferences(
            getString(R.string.com_ranseo_solaroid_LoginSaveKeyBool),
            Context.MODE_PRIVATE
        ) ?: return

        sharedPrefStr = this.context?.getSharedPreferences(
            getString(R.string.com_ranseo_solaroid_LoginSaveKeyStr),
            Context.MODE_PRIVATE
        ) ?: return


        val isLoginSave =
            sharedPrefBool.getBoolean(
                getString(R.string.com_ranseo_solaroid_LoginSaveKeyBool),
                false
            )

        val savedId =
            sharedPrefStr.getString(getString(R.string.com_ranseo_solaroid_LoginSaveKeyStr), null)
        if (isLoginSave) {
            viewModel.setSavedLoginId(savedId)
            binding.cbSaveId.isChecked = true
        } else {
            viewModel.setSavedLoginId(savedId)
            binding.cbSaveId.isChecked = false
        }
        ////


    }

//    override fun onStart() {
//        super.onStart()
//        if (kakaoToken) {
//            getKakaoUserInfo()
//
//        }
//    }

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
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[SolaroidLoginViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        functions = FirebaseFunctions.getInstance("asia-northeast3")

        providerBuilder = OAuthProvider.newBuilder("oidc.com.ranseo.solaroid")
        //선택사항
        //providerBuilder.addCustomParameter("login_hint", "user@example.com")
        //val scopes = arrayListOf("mail.read", "calendars.read")
        //providerBuilder.setScopes(scopes)

        coroutineScope = CoroutineScope(Dispatchers.IO)
        binding.btnKakaoLogout.setOnClickListener {
            UserApiClient().unlink { error ->
                if (error != null) {
                    Log.e(TAG, "연결 끊기 실패", error)
                } else {
                    Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨.")
                }
            }
        }


        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                SolaroidLoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.i(TAG, "로그인 성공 userId : ${auth.currentUser?.uid}")
                    if (auth.currentUser == null) auth.signOut()
                    else {
                        Log.i(TAG, "프로필설정 또는 메인컨텐츠 이동")
                        viewModel.setSavedLoginId(auth.currentUser?.email)
                        viewModel.isProfileAlready()
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

        navigateToOther()

        viewModel.isSaveId.observe(viewLifecycleOwner) {
            Log.i(TAG, "viewModel.isSaveId : ${it}")
            with(sharedPrefBool.edit()) {
                putBoolean(getString(R.string.com_ranseo_solaroid_LoginSaveKeyBool), it)
                apply()
            }

            putEmailSharedPref(it, viewModel.SavedLoginId.value)
        }


        viewModel.kakaoLogin.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let {
                //토큰 존재 여부 확인.
                checkKakaoToken()
                //checkPendingAuthResult()

            }
        }

        viewModel.kakaoUserInfo.observe(viewLifecycleOwner) {
            it?.let { user ->
                Log.i(
                    TAG, "카카오 유저 정보" +
                            "\n회원번호 : ${user.id}" +
                            "\n이메일 : ${user.kakaoAccount?.email}" +
                            "\n닉네임 : ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진 : ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )
            }
        }

        viewModel.customToken.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { customToken ->
                auth.signInWithCustomToken(customToken)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i(TAG, "signInWithCustomToken() success ${task.result.user?.uid}")
                        } else {
                            Log.e(TAG, "signInWithCustomToken() failure ${task.exception?.message}")
                        }
                    }
            }
        }


        return binding.root
    }

    private fun loginWithKakaoOIDC() {
        FirebaseManager.getAuthInstance()
            .startActivityForSignInWithProvider(requireActivity(), providerBuilder.build())
            .addOnSuccessListener { authResult ->

                val credential = authResult.credential
                if (credential !is OAuthCredential) return@addOnSuccessListener
                val accessToken = credential.accessToken
                val idToken = credential.idToken
                Log.i(TAG, "accessToken : ${accessToken}, idToken : ${idToken}")

                //getKakaoUserInfo()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "ERROR : $error")
            }
    }

    private fun checkPendingAuthResult() {
        Log.i(TAG, "checkPendingAuthResult()")
        FirebaseManager.getAuthInstance().pendingAuthResult?.addOnSuccessListener { authResult ->
            val credential = authResult.credential
            if (credential !is OAuthCredential) {
                Log.i(TAG, "checkPendingAuthResult() (credential !is OAuthCredential) ")
                loginWithKakaoOIDC()
                return@addOnSuccessListener
            }
            val accessToken = credential.accessToken
            val idToken = credential.idToken
            Log.i(
                TAG,
                "checkPendingAuthResult()  accessToken : ${accessToken}, idToken : ${idToken}"
            )
        }?.addOnFailureListener { error ->
            Log.e(TAG, "checkPendingAuthResult() ERROR : $error")
            loginWithKakaoOIDC()
        } ?: loginWithKakaoOIDC()


    }

    private fun navigateToOther() {
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
    }

    private fun checkKakaoToken() {
        if (AuthApiClient().hasToken()) {
            //UserApiClient의 AccessTokenInfo() API를 통해 액세스 토큰의 유효성 확인.
            UserApiClient().accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    //에러 발생 시, 액세스 토큰 및 리프레시 토큰이 유효하지 않아 사용자 로그인 필요
                    //각 에러에 맞는 처리 필요. 레퍼런스 참고.
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        //로그인 필요
                        loginWithKakao()
                    } else {
                        //기타에러
                    }
                } else if (tokenInfo != null) {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨) -> 사용자 로그인 불필요 + 해당 액세스 토큰으로 카카오 API 호출 가능
                    Log.i(
                        TAG, "토큰 정보 보기 성공" +
                                "\n회원번호 : ${tokenInfo.id}" +
                                "\n만료시간 : ${tokenInfo.expiresIn} 초"
                    )

//                    getCustomToken(AuthApiClient().tokenManagerProvider.manager.getToken()?.accessToken!!).addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Log.i(TAG, "getCustomToken() success : ${task.result} ")
//                        } else {
//                            Log.d(
//                                TAG,
//                                "getCustomToken() error : ${task.exception?.message} ${task.exception} "
//                            )
//                        }
//                    }

                    getCustomToken(AuthApiClient().tokenManagerProvider.manager.getToken()?.accessToken!!)
                    getKakaoUserInfo()
                }
            }
        } else {
            //로그인 필요
            loginWithKakao()
        }
    }

    private fun getKakaoUserInfo() {
        UserApiClient().me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(
                    TAG, "사용자 정보 요청 성공" +
                            "\n회원번호 : ${user.id}" +
                            "\n이메일 : ${user.kakaoAccount?.email}" +
                            "\n닉네임 : ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진 : ${user.kakaoAccount?.profile?.thumbnailImageUrl}" +
                            "\n인증정보 : ${user.kakaoAccount?.isEmailVerified}"
                )
                viewModel.setKakaoUser(user)
            }
        }
    }

    private fun loginWithKakao() {
        //카카오계정으로 로그인 공동 callback 구성
        //카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨.
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
//                getCustomToken(token.accessToken).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Log.i(TAG, "getCustomToken() success : ${task.result} ")
//                    } else {
//                        Log.d(
//                            TAG,
//                            "getCustomToken() error : ${task.exception?.message} ${task.exception} "
//                        )
//                    }
//                }
                getCustomToken(AuthApiClient().tokenManagerProvider.manager.getToken()?.accessToken!!)
                getKakaoUserInfo()
            }
        }

        //카카오톡 설치 여부
        val isSetUpKakaoTalk =
            UserApiClient().isKakaoTalkLoginAvailable(requireContext())
        UserApiClient().apply {
            if (isSetUpKakaoTalk) {
                loginWithKakaoTalk(requireContext()) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "로그인 실패", error)
                    } else if (token != null) {
                        Log.i(TAG, "로그인 성공 ${token.accessToken}")
                        getCustomToken(AuthApiClient().tokenManagerProvider.manager.getToken()?.accessToken!!)
//                        getCustomToken(token.accessToken).addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                Log.i(TAG, "getCustomToken() success : ${task.result} ")
//                            } else {
//                                Log.d(
//                                    TAG,
//                                    "getCustomToken() error : ${task.exception?.message} ${task.exception} "
//                                )
//                            }
//                        }
                    }
                }
            } else {
                loginWithKakaoAccount(requireContext(), callback = callback)
            }
        }
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
        putEmailSharedPref(viewModel.isSaveId.value == true, viewModel.SavedLoginId.value)

    }

    fun putEmailSharedPref(flag: Boolean, email: String?) {
        if (flag) {
            with(sharedPrefStr.edit()) {
                putString(getString(R.string.com_ranseo_solaroid_LoginSaveKeyStr), email)
                apply()
            }
            Log.i(TAG, "putEmailSharedPref : putString ${email}")
        } else {
            with(sharedPrefStr.edit()) {
                putString(getString(R.string.com_ranseo_solaroid_LoginSaveKeyStr), "")
                apply()
            }
            Log.i(TAG, "putEmailSharedPref : putString null")
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

        Log.i(TAG, "TYPE : ${type}")
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
                "com.ranseo.solaroid.login",
                true,
                null
            )
            .setHandleCodeInApp(true)
            .build()

        user.sendEmailVerification(actionCodeSetting)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "이메일 인증 메시지 전송")
                    Toast.makeText(this.context, "메일을 전송하였습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "이메일 인증 메시지 전송 실패")
                    Toast.makeText(this.context, "메일을 전송에 실패.네트워크 확인", Toast.LENGTH_SHORT).show()
                }
            }
    }


    //////////////////////////////////////////////////////////
    //functions

    private fun getCustomToken(kakaoAccessToken: String) {
        coroutineScope.launch {
            Log.i(TAG, "kakaoAccessToken : ${kakaoAccessToken}")
            val data = hashMapOf(
                "access_token" to kakaoAccessToken
            )

            functions.getHttpsCallable("kakaoToken")
                .call(data)
                .continueWith { task ->
                    val result = task.result?.data as String

                    result
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "${task.result}")
                        viewModel.setCustomToken(task.result)
                    } else {
                        Log.e(TAG, "${task.exception?.message}")
                    }
                }
        }
    }

    companion object {
        const val TAG = "로그인 프래그먼트"

        const val SEND_CHECK = "해당 이메일로 인증 메일을 보내시겠습니까?"
    }
}