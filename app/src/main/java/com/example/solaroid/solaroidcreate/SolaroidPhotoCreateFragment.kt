package com.example.solaroid.solaroidcreate

import android.app.Application
import android.content.ContentValues
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.solaroid.R
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidPhotoCreateBinding
import com.example.solaroid.firebase.RealTimeDatabaseViewModel
import com.example.solaroid.solaroidframe.SolaroidFrameFragmentContainer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class SolaroidPhotoCreateFragment : Fragment() {
    private lateinit var viewModelFactory: SolaroidPhotoCreateViewModelFactory
    private lateinit var viewModel: SolaroidPhotoCreateViewModel
    private lateinit var firebaseDBViewModel: RealTimeDatabaseViewModel

    private lateinit var application: Application

    private lateinit var binding: FragmentSolaroidPhotoCreateBinding

    private lateinit var firebaseDB: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth

    private var imageCapture: ImageCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSolaroidPhotoCreateBinding>(
            inflater,
            R.layout.fragment_solaroid_photo_create,
            container,
            false
        )

        application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        firebaseDB = Firebase.database
        firebaseAuth = FirebaseAuth.getInstance()

        viewModelFactory =
            SolaroidPhotoCreateViewModelFactory(dataSource.photoTicketDao, application)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[SolaroidPhotoCreateViewModel::class.java]
        firebaseDBViewModel =
            ViewModelProvider(requireActivity())[RealTimeDatabaseViewModel::class.java]

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.cameraConverter.observe(viewLifecycleOwner, Observer {
            startCamera(it)
        })


        viewModel.startImageCapture.observe(viewLifecycleOwner, Observer {
            if (it) {
                captureImage()
                viewModel.stopImageCapture()
            }
        })

        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let { photo ->
                val user = firebaseAuth.currentUser!!
                firebaseDBViewModel.setValueInPhotoTicket(photo, user)
            }


        })


        return binding.root
    }

    private fun startCamera(cameraConverter: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(application)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = if (!cameraConverter) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }


            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "lifecycle Bind Failed", exc)
            }

        }, ContextCompat.getMainExecutor(application))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        val name =
            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "pictures/Solaroid")
            }
        }


        //fragment에서 contentResolver 쓰려면 activity에서 끌고와야함
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            application.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(application),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri!!
                    val msg = "Photo Capture Succeeded : $savedUri"
                    Toast.makeText(application, "사진이 성공적으로 찍혔습니다.", Toast.LENGTH_SHORT).show()
                    viewModel.setCapturedImageUri(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "Photo Capture Failed : ${exception.message}")
                }

            }
        )

    }

    companion object {
        private const val TAG = "생성"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}