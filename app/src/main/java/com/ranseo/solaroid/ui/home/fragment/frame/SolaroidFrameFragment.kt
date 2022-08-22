package com.ranseo.solaroid.ui.home.fragment.frame


import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.ranseo.solaroid.R
import com.ranseo.solaroid.adapter.OnFrameLongClickListener
import com.ranseo.solaroid.adapter.SolaroidFrameAdapter
import com.ranseo.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.ranseo.solaroid.dialog.ListSetDialogFragment
import com.ranseo.solaroid.ui.home.fragment.gallery.PhotoTicketFilter
import com.ranseo.solaroid.room.SolaroidDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ranseo.solaroid.adapter.OnFrameShareListener
import com.ranseo.solaroid.ui.home.fragment.create.SolaroidPhotoCreateFragment
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SolaroidFrameFragment : Fragment(), ListSetDialogFragment.ListSetDialogListener {

    companion object {
        const val TAG = "프레임 컨테이너"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    private lateinit var binding: FragmentSolaroidFrameContainerBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var onPageChangeCallback: ViewPager2.OnPageChangeCallback

    private val args by navArgs<SolaroidFrameFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val args = requireArguments()
//        val filter = SolaroidFrameFragmentArgs.fromBundle(args).filter
//        val photoTicket =  SolaroidFrameFragmentArgs.fromBundle(args).photoTicket
//
        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        val filter = args.filter
        val photoTicket = args.photoTicket
        val albumId = args.albumId
        val albumKey = args.albumKey

        viewModelFactory = SolaroidFrameViewModelFactory(
            dataSource.photoTicketDao,
            application,
            PhotoTicketFilter.convertStringToFilter(filter),
            photoTicket,
            albumId,
            albumKey
        )

        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[SolaroidFrameViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_solaroid_frame_container,
            container,
            false
        )

        setHasOptionsMenu(true)

        viewPager = binding.viewpager

        val adapter = SolaroidFrameAdapter(OnFrameLongClickListener {
            showListDialog()
        }, OnFrameShareListener { bitmap ->
            setBitmapImage(bitmap)
        })


        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        registerOnPageChangeCallback(adapter)

        viewModel.startPosition.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { pos ->
                Log.i(TAG, "startPosition : ${pos}")
                binding.viewpager.setCurrentItem(pos, false)
            }
        }


        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            list?.let {
                Log.i(TAG, "photoTickets : ${list}")
//                viewModel.setPhotoTicketSize(it.size)
                adapter.submitList(list)
                binding.viewpager.adapter = adapter

                viewModel.refreshPhotoTicket()
            }
        }

        viewModel.shareImage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                val bitmap = viewModel.currBitmap.value
                bitmap?.let {
                    Log.i(TAG, "shareImage.observe ${bitmap}")
                    makeImage(bitmap)

                }
            }
        }

        viewModel.currBitmap.observe(viewLifecycleOwner) {
            it?.let { bitmap ->
                Log.i(TAG, "currBitmap.observe ${bitmap}")

            }

        }


        observeCurrentPosition()
        observeCurrentPhoto()
        observeFavorite()

        navigateToOtherFragment()



        setOnItemSelectedListener(binding.frameBottomNavi)


        return binding.root
    }


    /**
     * viewModel의 favorite 프로퍼티 관찰.
     * 1.현재 viewPager's page 내 photoTicket의 favorite 값에 따라 bottomNavi의 menuItem "즐겨찾기" 의 Icon을 변경.
     * */
    private fun observeFavorite() {
        viewModel.favorite.observe(viewLifecycleOwner) { favor ->
            favor?.let {
                Log.d(TAG, "viewModel.favorite.observe  : ${favor}")
                //getItem은 오류 findItem이랑 다른듯.!
                val menuItem: MenuItem =
                    binding.frameBottomNavi.menu.findItem(R.id.favorite)
                menuItem.setIcon(if (!it) R.drawable.ic_favorite_false else R.drawable.ic_favorite_true)
                Log.d(TAG, "Success")
            }
        }
    }

    /**
     * 포토티켓의 삭제 및 [즐겨찾기 프래그먼트]에서의 즐겨찾기 해제로 인해 viewPager의 포토티켓이 사라지는 경우가 발생.
     * 이때 viewPager의 0번째 위치에서 포토티켓이 사라진다면, 뒤에 있던 포토티켓이 0번째 위치로 이동하게 된다.
     * viewPager의 onPagedSelected는 현재 위치가 변경되지 않았기 때문에 감지하지 못함. 따라서 현재 포토티켓을 캐치하지 못하는 문제발생.
     * 이를 방지하기 위해 FrameViewModel에 Position이라는 변수를 만들고, "삭제 및 즐겨찾기 해제"가 발생할 때 현재 위치의
     * 포토티켓을 캐치하여 app이 정상적으로 현 포토티켓 상황을 유저들에게 전달할 수 있도록 함.
     * */
    private fun observeCurrentPosition() {
        viewModel.currentPosition.observe(viewLifecycleOwner, Observer { pos ->
            pos?.let {
                Log.i(TAG, "currentPosition.observe pos : ${it}")
                viewModel.setCurrentPhotoTicket(pos)
            }
        })
    }

    /**
     * viewModel의 photoTicket 프로퍼티를 관찰.
     * 현재 기능 : photoTicket 데이터 변경 시, viewModel의 setCurrentFavorite 함수 호출 후, 현재 photoTicket의 favorite 값 인자로 넘겨줌.
     **/
    private fun observeCurrentPhoto() {
        viewModel.currPhotoTicket.observe(viewLifecycleOwner) {
            it?.let { photoTicket ->
                viewModel.setCurrentFavorite(photoTicket.favorite)
                viewModel.setStartPhotoTicket(photoTicket)
                //Toast.makeText(this.context, "현재 포토티켓 : ${photoTicket}", Toast.LENGTH_LONG).show()
                Log.i(
                    TAG,
                    "observeCurrentPhoto()  : ${photoTicket}"
                )
            }
            if (it == null) viewModel.setCurrentFavorite(false)
        }
    }


    /**
     * gallery Fragment에서 viewModel naviTo 라이브 객체를 관찰하여
     * 원하는 Fragment로 이동하기 위한 코드를 모아놓은 함수
     * */
    private fun navigateToOtherFragment() {


        viewModel.naviToEditFrag.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { key ->
                val albumId = viewModel.albumId
                val albumKey = viewModel.albumKey
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToEdit(
                        key,
                        albumId,
                        albumKey
                    )
                )
            }
        }


    }


    private fun registerOnPageChangeCallback(
        adapter: SolaroidFrameAdapter
    ) {


        onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.i(TAG, "onPageSelected")
                if (adapter.itemCount > 0) {
                    viewModel.setCurrentPosition(position)
                } else {
                    viewModel.setCurrentPosition(-1)
                }
            }
        }
        viewPager.registerOnPageChangeCallback(onPageChangeCallback)
        Log.i(TAG, "registerOnPageChangeCallback : 등록")

    }


    /**
     * bottomNavigation의 item Selection별 기능 구현 함수.
     * 1.선택한 item이 favorite 일 때, viewModel의 favorite 값의 !favorite 값을 viewModel의 togglePhotoTicketFavorite() 호출하여 인자로 넘겨줌.
     * */
    private fun setOnItemSelectedListener(
        botNavi: BottomNavigationView,
    ) {
        botNavi.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.favorite -> {
                    val favorite = viewModel.currPhotoTicket.value?.favorite
                    Log.i(TAG, "favorite ${favorite}")
                    if (favorite != null) viewModel.updatePhotoTicketFavorite()
                    true
                }
                R.id.edit -> {
                    val id = viewModel.currPhotoTicket.value?.id
                    if (id != null) {
                        viewModel.navigateToEdit(id)

                    }
                    true
                }
                R.id.share -> {
                    viewModel.sharePhotoTicket()
                    true
                }
                else -> false
            }

        }
    }


    /**
     * 포토티켓을 LongClick했을 때 AlertDialog가 표시.
     * dialog의 항목들에 대해 각각 코드 구현.
     * */
    override fun onDialogListItem(
        dialog: DialogFragment,
        position: Int,
    ) {
        val key = viewModel.currPhotoTicket.value!!.id
        when (position) {
            //delete
            0 -> {
                viewModel.deletePhotoTicket(key)
                dialog.dismiss()
            }
            //수 정
            1 -> {
                viewModel.navigateToEdit(key)
                dialog.dismiss()
            }
        }
    }

    private fun showListDialog() {
        val newDialogFragment = ListSetDialogFragment(R.array.frame_long_click_dialog_items, this)
        newDialogFragment.show(parentFragmentManager, "ListDialog")
    }

    private fun setBitmapImage(bitmap: Bitmap) {
        viewModel.setCurrPhotoTicketBitmap(bitmap)
    }


    private fun makeImage(bitmap: Bitmap) {
        val name =
            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Solaroid")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }


        //fragment에서 contentResolver 쓰려면 activity에서 끌고와야함

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val item: Uri = requireActivity().contentResolver.insert(collection, contentValues)!!


        try{
            requireActivity().contentResolver.openFileDescriptor(item, "w", null).use {
                FileOutputStream(it!!.fileDescriptor).use { outputStream ->
                    Log.i(TAG,"outputStream  : ${outputStream}")
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            requireActivity().contentResolver.update(item, contentValues, null, null)


        } catch (error:Exception) {
            Log.e(TAG,"makeImage() error: ${error}")
        } catch (error:IOException) {
            error.printStackTrace()
        } catch (error: FileNotFoundException) {
            error.printStackTrace()
        }

    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop : 등록 해제")
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy : 등록 해제")
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

}