package com.ranseo.solaroid.ui.login

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidLoginViewModel
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidProfileViewModel

class LoginObserver(val registry: ActivityResultRegistry, val oneTapClient: SignInClient, val viewModel: SolaroidLoginViewModel) : DefaultLifecycleObserver {
    lateinit var getContent: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(owner: LifecycleOwner) {
        Log.i(TAG,"onCreate() : create")
        getContent = registry.register(REQ_ONE_TAP, ActivityResultContracts.StartIntentSenderForResult()) { result ->
            result.data?.let{ data ->
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken

                when{
                    idToken != null -> {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        viewModel.setCredential(firebaseCredential)
                        Log.i(TAG,"signInWithCredential:Success")
                    }

                    else -> {
                        Log.d(TAG, "No ID Token!")
                        viewModel.setLoginErrorType(SolaroidLoginViewModel.LoginErrorType.CREDENTIALERROR)
                    }
                }
            }

        }
    }

    fun startIntentSenderResult(result:BeginSignInResult) {
        val intentSenderResult = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        getContent.launch(intentSenderResult)

    }

    companion object {
        private const val REQ_ONE_TAP = "KEY"
        private const val TAG = "LoginObserver"
    }
}