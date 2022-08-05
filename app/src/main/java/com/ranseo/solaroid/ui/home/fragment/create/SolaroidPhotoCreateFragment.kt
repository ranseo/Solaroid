package com.ranseo.solaroid.ui.home.fragment.create

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ranseo.solaroid.R
import com.ranseo.solaroid.room.SolaroidDatabase
import com.ranseo.solaroid.databinding.FragmentSolaroidPhotoCreateBinding
import java.text.SimpleDateFormat
import java.util.*


class SolaroidPhotoCreateFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var viewModelFactory: SolaroidPhotoCreateViewModelFactory
    private lateinit var viewModel: SolaroidPhotoCreateViewModel

    private lateinit var application: Application

    private lateinit var binding: FragmentSolaroidPhotoCreateBinding

    private lateinit var backPressCallback :OnBackPressedCallback

    private var imageCapture: ImageCapture? = null
    private var CameraProvider :  ProcessCameraProvider? = null

    private var lensFacing : Int = CameraSelector.LENS_FACING_BACK


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


        viewModelFactory =
            SolaroidPhotoCreateViewModelFactory(dataSource.photoTicketDao, application)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[SolaroidPhotoCreateViewModel::class.java]


        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        //albumNameList의 값이 관찰되면 해당 값을 이용해 Spinner 만들기
        viewModel.albumNameList.observe(viewLifecycleOwner) { list ->
            if(!list.isNullOrEmpty()) {
                val defaultIdx=  list.indexOf(viewModel.albumName)
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    list
                ).also{ adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerAlbum.adapter = adapter
                    binding.spinnerAlbum.onItemSelectedListener = this
                    binding.spinnerAlbum.setSelection(defaultIdx)
                }
            }
        }



        viewModel.cameraConverter.observe(viewLifecycleOwner){
            CameraProvider?.unbindAll()
            startCamera(it)
        }



        viewModel.startImageCapture.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let {
                captureImage()
            }
        }


        return binding.root
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        backPressCallback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                viewModel.navigateToFrame()
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(this,backPressCallback)
//    }

    /**
     * 일반 카메라 Preview 실행
     * */
    private fun startCamera(cameraConverter: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(application)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            CameraProvider = cameraProvider
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = if (!cameraConverter) {
                lensFacing = CameraSelector.LENS_FACING_BACK
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                lensFacing = CameraSelector.LENS_FACING_FRONT
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

    /**
     * extension Api 사용한 카메라실행
     * */

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

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal =  lensFacing == CameraSelector.LENS_FACING_FRONT
        }


        //fragment에서 contentResolver 쓰려면 activity에서 끌고와야함
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            application.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            .setMetadata(metadata)
            .build()

        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(application),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri!!
                    val msg = "Photo Capture Succeeded : $savedUri"
                    Log.i(TAG, "msg : $msg")
                    //Toast.makeText(application, "사진이 성공적으로 찍혔습니다.", Toast.LENGTH_SHORT).show()
                    viewModel.setCapturedImageUri(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "Photo Capture Failed : ${exception.message}")
                }

            }
        )

    }

    override fun onDestroy() {
        CameraProvider?.unbindAll()
        super.onDestroy()
    }



    companion object {
        private const val TAG = "생성프래그먼트"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    //Spinner ItemSelected
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        viewModel.setWhichAlbum(p2)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}