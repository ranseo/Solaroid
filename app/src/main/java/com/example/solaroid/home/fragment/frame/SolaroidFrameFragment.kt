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
import com.example.solaroid.dialog.FilterDialogFragment
import com.example.solaroid.dialog.ListSetDialogFragment
import com.example.solaroid.firebase.FirebaseManager
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

        val (photoKey, filter) = args.key

        viewModelFactory = SolaroidFrameViewModelFactory(
            dataSource.photoTicketDao,
            application,
            photoKey,
            PhotoTicketFilter.convertStringToFilter(filter)
        )
        viewModel = ViewModelProvider(
            this.requireActivity(),
            viewModelFactory
        )[SolaroidFrameViewModel::class.java]

        val adapter = SolaroidFrameAdapter(OnFrameLongClickListener {
            showListDialog(viewModel)
        })


        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.photoTickets.observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.submitList(list)
            }
        }


        /**
         * viewModel의 photoTicket 프로퍼티를 관찰.
         * 현재 기능 : photoTicket 데이터 변경 시, viewModel의 setCurrentFavorite 함수 호출 후, 현재 photoTicket의 favorite 값 인자로 넘겨줌.
         **/
        viewModel.currPhotoTicket.observe(viewLifecycleOwner, Observer {
            it?.let { photoTicket ->
                viewModel.setCurrentFavorite(photoTicket.favorite)
                //Toast.makeText(this.context, "현재 포토티켓 : ${photoTicket}", Toast.LENGTH_LONG).show()
                Log.i(
                    SolaroidFrameFragment.TAG,
                    "viewModel.currPhotoTicket.observe : ${photoTicket}"
                )
            }
            if (it == null) viewModel.setCurrentFavorite(false)
        })

        /**
         * viewModel의 favorite 프로퍼티 관찰.
         * 1.현재 viewPager's page 내 photoTicket의 favorite 값에 따라 bottomNavi의 menuItem "즐겨찾기" 의 Icon을 변경.
         * */
        viewModel.favorite.observe(viewLifecycleOwner, Observer { favor ->
            favor?.let {
                Log.d(SolaroidFrameFragment.TAG, "viewModel.favorite.observe  : ${favor}")
                //getItem은 오류 findItem이랑 다른듯.!
                val menuItem: MenuItem =
                    binding.frameBottomNavi.menu.findItem(R.id.favorite)
                menuItem.setIcon(if (!it) R.drawable.ic_favorite_false else R.drawable.ic_favorite_true)
                Log.d(SolaroidFrameFragment.TAG, "Success")
            }
        })



        navigateToOtherFragment()

        setOnItemSelectedListener(binding.frameBottomNavi)
        return binding.root
    }


    /**
     * gallery Fragment에서 viewModel naviTo 라이브 객체를 관찰하여
     * 원하는 Fragment로 이동하기 위한 코드를 모아놓은 함수
     * */
    private fun navigateToOtherFragment() {
        viewModel.naviToCreateFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToCreate()
                )
            }
        })


        viewModel.naviToEditFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { key ->
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToEdit(key)
                )
            }
        })


        viewModel.naviToAddFrag.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameToAdd()
                )
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
        botNavi.setOnItemSelectedListener { it ->
            when (it.itemId) {
                R.id.favorite -> {
                    val favorite = viewModel.currPhotoTicket.value?.favorite
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