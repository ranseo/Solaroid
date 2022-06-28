package com.example.solaroid.home.fragment.frame


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.OnFrameLongClickListener
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.databinding.FragmentSolaroidFrameContainerBinding
import com.example.solaroid.dialog.ListSetDialogFragment
import com.example.solaroid.home.fragment.gallery.PhotoTicketFilter
import com.example.solaroid.room.SolaroidDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView

class SolaroidFrameFragment : Fragment(), ListSetDialogFragment.ListSetDialogListener {

    companion object {
        const val TAG = "프레임 컨테이너"
    }

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    private lateinit var binding: FragmentSolaroidFrameContainerBinding

    private val args by navArgs<SolaroidFrameFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val args = requireArguments()
//        val filter = SolaroidFrameFragmentArgs.fromBundle(args).filter
//        val photoTicket =  SolaroidFrameFragmentArgs.fromBundle(args).photoTicket
//

        val filter = args.filter
        val photoTicket = args.photoTicket
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

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        val filter = args.filter

        Log.i(TAG, "FrameFragment 시작")

        viewModelFactory = SolaroidFrameViewModelFactory(
            dataSource.photoTicketDao,
            application,
            PhotoTicketFilter.convertStringToFilter(filter)
        )
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[SolaroidFrameViewModel::class.java]

        val adapter = SolaroidFrameAdapter(OnFrameLongClickListener {
            showListDialog(viewModel)
        })




        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewpager.adapter = adapter


        viewModel.startPosition.observe(viewLifecycleOwner) { pos ->
            pos?.let {
                binding.viewpager.setCurrentItem(pos,false)
                registerOnPageChangeCallback(binding.viewpager, adapter)
            }
        }


        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            list?.let {
                Log.i(TAG, "photoTickets : ${list}")
                viewModel.setPhotoTicketSize(it.size)
                adapter.submitList(list)
            }
        }




        observeCurrentPhoto()
        observeFavorite()
        navigateToOtherFragment()
        observeCurrentPosition(adapter)
        refreshCurrentPosition(binding.viewpager)

        setOnItemSelectedListener(binding.frameBottomNavi)


        return binding.root
    }

    /**
     * viewModel의 favorite 프로퍼티 관찰.
     * 1.현재 viewPager's page 내 photoTicket의 favorite 값에 따라 bottomNavi의 menuItem "즐겨찾기" 의 Icon을 변경.
     * */
    private fun observeFavorite() {
        viewModel.favorite.observe(viewLifecycleOwner){ favor ->
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
     * viewModel의 photoTicket 프로퍼티를 관찰.
     * 현재 기능 : photoTicket 데이터 변경 시, viewModel의 setCurrentFavorite 함수 호출 후, 현재 photoTicket의 favorite 값 인자로 넘겨줌.
     **/
    private fun observeCurrentPhoto() {
        viewModel.currPhotoTicket.observe(viewLifecycleOwner){
            it?.let { photoTicket ->
                viewModel.setCurrentFavorite(photoTicket.favorite)
                //Toast.makeText(this.context, "현재 포토티켓 : ${photoTicket}", Toast.LENGTH_LONG).show()
                Log.i(
                    TAG,
                    "viewModel.currPhotoTicket.observe : ${photoTicket}"
                )
            }
            if (it == null) viewModel.setCurrentFavorite(false)
        }
    }

    /**
     * 포토티켓의 삭제 및 [즐겨찾기 프래그먼트]에서의 즐겨찾기 해제로 인해 viewPager의 포토티켓이 사라지는 경우가 발생.
     * 이때 viewPager의 0번째 위치에서 포토티켓이 사라진다면, 뒤에 있던 포토티켓이 0번째 위치로 이동하게 된다.
     * viewPager의 onPagedSelected는 현재 위치가 변경되지 않았기 때문에 감지하지 못함. 따라서 현재 포토티켓을 캐치하지 못하는 문제발생.
     * 이를 방지하기 위해 FrameViewModel에 Position이라는 변수를 만들고, "삭제 및 즐겨찾기 해제"가 발생할 때 현재 위치의
     * 포토티켓을 캐치하여 app이 정상적으로 현 포토티켓 상황을 유저들에게 전달할 수 있도록 함.
     * */
    private fun observeCurrentPosition(
        adapter: SolaroidFrameAdapter
    ) {
        viewModel.currentPosition.observe(viewLifecycleOwner, Observer { pos ->
            //현재 위치가 0보다 커야한다. (음수가 되는 상황은 발생하지 않음)
            //현재 위치가 adapter내의 전체 아이템의 크기 수보다 작아야 한다. (아이템의 크기를 넘어서 존재할 수 없음)
            pos?.let {
                Log.i(TAG, "currentPosition.observe pos : ${it}")
                if (it > adapter.itemCount) {
                    viewModel.setCurrentFavorite(false)
                }
            }
        })
    }

    /**
     * 포토티켓이 viewPager에서 사라졌을 때, adapter에 제공되는 PhotoTicket List의 사이즈를 담고 있는 LiveData
     * "photoTicketsSize 변수를 관찰. -> 변경이 있을 때마다, 현재 viewpager의 위치를 새롭게 setCurrentPosition의
     * 매개변수로 넘긴다.
     * */
    private fun refreshCurrentPosition(
        viewPager: ViewPager2
    ) {
        viewModel.photoTicketsSize.observe(viewLifecycleOwner) { size ->
            if (size != null) {
                if (size > 0) {

                    val position =
                        if(size <= viewPager.currentItem) viewPager.currentItem - 1 else viewPager.currentItem
                    Log.i(TAG, "refreshCurrentPosition photoTicket Id: ${position} : ${size}")
                    viewModel.setCurrentPosition(position)
                }
            }
        }

    }


    /**
     * gallery Fragment에서 viewModel naviTo 라이브 객체를 관찰하여
     * 원하는 Fragment로 이동하기 위한 코드를 모아놓은 함수
     * */
    private fun navigateToOtherFragment() {
        viewModel.naviToCreateFrag.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToCreate()
                )
            }
        }


        viewModel.naviToEditFrag.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { key ->
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToEdit(key)
                )
                viewModel.refreshPhotoTicket()
            }
        }


        viewModel.naviToAddFrag.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToAdd()
                )
            }
        }
    }


    private fun registerOnPageChangeCallback(
        viewPager: ViewPager2,
        adapter: SolaroidFrameAdapter
    ) {

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.i(TAG, "onPageSelected")
                if (adapter.itemCount > 0) {
                    viewModel.setCurrentPosition(position)
                } else {
                    viewModel.setCurrentPosition(-1)
                }
            }
        })
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
                    if (id != null) viewModel.navigateToEdit(id)
                    true

                }

                R.id.add -> {
                    viewModel.navigateToAdd()
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
        viewModel: SolaroidFrameViewModel
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

    private fun showListDialog(viewModel: SolaroidFrameViewModel) {
        val newDialogFragment = ListSetDialogFragment(this, viewModel)
        newDialogFragment.show(parentFragmentManager, "ListDialog")
    }


}