package com.ranseo.solaroid.ui.home.fragment.frame


import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.content.getSystemService
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
import com.ranseo.solaroid.MyFileProvider
import com.ranseo.solaroid.adapter.OnFrameShareListener
import java.io.*
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

        val shareFront: (front: Bitmap, pos: Int) -> Unit = { front, pos ->
            viewModel.setCurrFrontBitmap(front, pos)
        }

        val shareBack: (back: Bitmap, pos: Int) -> Unit = { back, pos ->
            viewModel.setCurrBackBitmap(back, pos)
        }

        val adapter = SolaroidFrameAdapter(OnFrameLongClickListener {
            showListDialog()
        }, OnFrameShareListener(shareFront, shareBack))
        binding.viewpager.adapter = adapter

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        
        registerOnPageChangeCallback(adapter)

        viewModel.startPosition.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { pos ->
                // Log.i(TAG, "startPosition : ${pos}")

                binding.viewpager.setCurrentItem(pos, false)
                binding.frameBottomNavi.menu.findItem(R.id.favorite).isEnabled = true
            }
        }

        var cnt = 1
        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            list?.let { photoTicket ->
                binding.frameBottomNavi.menu.findItem(R.id.favorite).isEnabled = false
                Log.i(TAG, "photoTickets : ${list}")
                val currPhotoTicket = viewModel.currPhotoTicket.value
                currPhotoTicket?.let { photoTicket ->
                    viewModel.setStartPhotoTicket(photoTicket)
                }
//                viewModel.setPhotoTicketSize(it.size)
                repeat(cnt) {
                    viewModel.refreshBimtaps(photoTicket.size)
                    cnt--
                }

                adapter.submitList(list)
//                binding.viewpager.adapter = adapter

                viewModel.refreshPhotoTicket()
                viewModel.refreshFavorite()
            }
        }

//        viewModel.photoTicketSize.observe(viewLifecycleOwner) {
//            it?.let{ size ->
//                viewModel.setBitmapArray(size)
//            }
//        }

        viewModel.shareImage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                val currPos = viewModel.currentPosition.value!!


                val frontBitmap = viewModel.currFrontBitmaps[currPos]
                val backBitmap = viewModel.currBackBitmaps[currPos]

                // Log.i(TAG, "currPos : ${currPos}, frontBitmap  : ${frontBitmap}, backBitmap  : ${backBitmap}")

                val imageUris: ArrayList<Uri> = arrayListOf()
                frontBitmap?.let {
                    imageUris.add(makeCacheDir1(frontBitmap))
                }

                backBitmap?.let {
                    imageUris.add(makeCacheDir1(backBitmap))
                }

                shareIntent(imageUris)

//                val shareIntent = Intent().apply {
//                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                    action = Intent.ACTION_SEND_MULTIPLE
//                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, )
//                    type = "image/png"
//                }
//
//
//                if (shareIntent.resolveActivity(this.requireActivity().packageManager) != null) {
//                    startActivity(Intent.createChooser(shareIntent, "이미지 공유"))
//                }
            }
        }


        observeCurrentPosition()
        observeCurrentPhoto()
        observeFavorite()

        navigateToOtherFragment()



        setOnItemSelectedListener(binding.frameBottomNavi)

        //1

        return binding.root
    }



    /**
     * viewModel의 favorite 프로퍼티 관찰.
     * 1.현재 viewPager's page 내 photoTicket의 favorite 값에 따라 bottomNavi의 menuItem "즐겨찾기" 의 Icon을 변경.
     * */
    private fun observeFavorite() {
        viewModel.favorite.observe(viewLifecycleOwner) { favor ->
            favor?.let {
                Log.i(TAG, "viewModel.favorite.observe  : ${favor}")
                //getItem은 오류 findItem이랑 다른듯.!
                val menuItem: MenuItem =
                    binding.frameBottomNavi.menu.findItem(R.id.favorite)
                menuItem.setIcon(if (!it) R.drawable.ic_favorite_false else R.drawable.ic_favorite_true)
                Log.i(TAG, "Success")
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
                //Log.i(TAG, "currentPosition.observe pos : ${it}")
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
                //Log.i(TAG, "observeCurrentPhoto()  : ${photoTicket}")
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
                //Log.i(TAG, "onPageSelected")
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


    private fun makeCacheDir(bitmap: Bitmap) {
        val imagePath = File(requireActivity().cacheDir, "my_images")
        imagePath.mkdirs()
        val stream = FileOutputStream(imagePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
    }

    private fun clipData(uri:Uri) {
        val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val lastName = uri.lastPathSegment
        Log.i(TAG, "clipData LastName = ${lastName}")

        uri.

    }

    private fun shareIntent(uris: ArrayList<Uri>) {
        val shareIntent = Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            type = "image/png"
        }


        if (shareIntent.resolveActivity(this.requireActivity().packageManager) != null) {
            startActivity(Intent.createChooser(shareIntent, "이미지 공유"))
        }
    }

    private fun makeCacheDir1(bitmap: Bitmap): Uri {
        val imagePath = File(requireActivity().cacheDir, "my_images")
        imagePath.mkdirs()
        val fileName =
            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis())
        val newFile = File(imagePath, "${fileName}.png")

        val uri = getUriForFile(requireContext(), "com.ranseo.solaroid.fileprovider", newFile)

        val packageName = requireContext().packageName
        Log.i(TAG, "packageName : ${packageName}")
        requireActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        try {
            Log.i(TAG,"URI : ${uri}")
            requireActivity().contentResolver.openFileDescriptor(uri, "w", null).use {
                FileOutputStream(it!!.fileDescriptor).use { outputStream ->
                    Log.i(TAG,"outputStream: ${outputStream}")
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }
            Log.i(TAG, "success")
        } catch (error: Exception) {
            Log.e(TAG, "makeCacheDir() error: ${error}")
        } catch (error: IOException) {
            error.printStackTrace()
        } catch (error: FileNotFoundException) {
            error.printStackTrace()
        }


        requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return uri


    }

    private fun shareImagesInCacheDir(file: File) {
        val imagePath = File(requireActivity().cacheDir, "my_images")

    }


    private fun makeImageInMediaStore(bitmap: Bitmap): Uri {
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

        try {
            requireActivity().contentResolver.openFileDescriptor(item, "w", null).use {
                FileOutputStream(it!!.fileDescriptor).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            requireActivity().contentResolver.update(item, contentValues, null, null)

        } catch (error: Exception) {
            Log.e(TAG, "makeImage() error: ${error}")
        } catch (error: IOException) {
            error.printStackTrace()
        } catch (error: FileNotFoundException) {
            error.printStackTrace()
        }

        return item
    }

//    override fun onStop() {
//        super.onStop()
//        Log.i(TAG, "onStop : 등록 해제")
//        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
//    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy : 등록 해제")
        viewModel.recycleBitmap()
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

}