package com.ranseo.solaroid

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.repositery.log.LogRepositery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        var keyHash = Utility.getKeyHash(this)
        Log.i("GlobalApplication", "$keyHash")


        Log.i("GlobalApplication", "${BuildConfig.KAKAO_NATIVE_APP_KEY}")
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

    }
}