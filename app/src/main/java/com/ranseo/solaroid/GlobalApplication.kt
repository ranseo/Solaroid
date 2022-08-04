package com.ranseo.solaroid

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        var keyHash = Utility.getKeyHash(this)
//        Log.i("GlobalApplication", "$keyHash")
        KakaoSdk.init(this, "71eb0d1f916dfd2a0f2786e2c9fff1d6")

    }
}