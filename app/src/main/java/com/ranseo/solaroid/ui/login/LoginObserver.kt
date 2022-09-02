package com.ranseo.solaroid.ui.login

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidProfileViewModel

class LoginObserver(val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
    lateinit var getContent: ActivityResultLauncher<String>
    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register("KEY", ActivityResultContracts.StartIntentSenderForResult) {

        }
    }

    fun selectImage() {
        getContent.launch("image/*")

    }
}