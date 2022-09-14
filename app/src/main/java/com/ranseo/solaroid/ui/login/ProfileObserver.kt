package com.ranseo.solaroid.ui.login

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ranseo.solaroid.ui.login.viewmodel.SolaroidProfileViewModel
import com.ranseo.solaroid.utils.FileUtils

class ProfileObserver(val registry: ActivityResultRegistry, val viewModel: SolaroidProfileViewModel) : DefaultLifecycleObserver {
    lateinit var getContent: ActivityResultLauncher<String>
    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register("key", owner, ActivityResultContracts.GetContent()) { uri ->
            uri?.let{
                FileUtils.makeCacheDir()
                viewModel.setProfileUrl(it)
            }
        }
    }

    fun selectImage() {
        getContent.launch("image/*")
    }
}