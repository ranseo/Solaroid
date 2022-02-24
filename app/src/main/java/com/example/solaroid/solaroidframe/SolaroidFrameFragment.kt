package com.example.solaroid.solaroidframe

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.OnClickListener
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.databinding.FragmentSolaroidFrameBinding

open class SolaroidFrameFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var viewModelFactory: SolaroidFrameViewModelFactory
    private lateinit var viewModel: SolaroidFrameViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameBinding>(
            inflater,
            R.layout.fragment_solaroid_frame,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)

        viewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        viewModel = ViewModelProvider(
            this.requireActivity(),
            viewModelFactory
        )[SolaroidFrameViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = SolaroidFrameAdapter(OnClickListener { photoTicketKey ->
            viewModel.naviToDetail(photoTicketKey)
        })

        binding.viewpager.adapter = adapter


        //adapter click event 설정
        navigateToDetailFragment(viewModel)

        viewModel.navigateToFrameFrag.observe(viewLifecycleOwner, Observer{
            if(it) {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameFragmentToFrameFavoriteFragment()
                )
                viewModel.doneNaviToFrameFrag()
            }
        })

        //현재 페이지의 포토티켓 즐겨찾기 여부를 확인.
        observePhotoTicket(viewModel)

        //popUp Menu 호출
        observePopupMenu(viewModel,binding)

        //얘도정리
        observeFavorite(viewModel,binding)


        //02.21 얘도정리.
        registerOnPageChangeCallback(viewModel, binding, adapter)


        //02.21 이거정리.
        setOnItemSelectedListener(viewModel, binding)

        return binding.root
    }

    /**
     * bottomNavigation의 item Selection별 기능 구현 함수.
     * 1.선택한 item이 favorite 일 때, viewModel의 favorite 값의 !favorite 값을 viewModel의 togglePhotoTicketFavorite() 호출하여 인자로 넘겨줌.
     * */
    open fun setOnItemSelectedListener(viewModel: SolaroidFrameViewModel,binding: FragmentSolaroidFrameBinding) {
        binding.fragmentFrameBottomNavi.setOnItemSelectedListener {
            if (it.itemId == R.id.favorite) {
                val favorite = viewModel.favorite.value
                if (favorite != null) {
                    if (favorite) viewModel.togglePhotoTicketFavorite(false) else viewModel.togglePhotoTicketFavorite(
                        true
                    )
                }

            }
            false
        }
    }

    /**
     * binding된 viewPager의 selected page의 PhotoTicket 객체를 추출. -> viewModel의 setCurrentPhotoTicket() 호출하여 인자로 넘겨줌.
     * */
    protected open fun registerOnPageChangeCallback(
        viewModel: SolaroidFrameViewModel,
        binding: FragmentSolaroidFrameBinding,
        adapter: SolaroidFrameAdapter
    ) {
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val photoTicket = adapter.getPhotoTicket(position)
                Log.d("FrameFragment", "onPageSelected, photoTicket id : ${photoTicket.id}")
                viewModel.setCurrentPhotoTicket(photoTicket)
            }
        })
    }

    /**
     * viewModel의 favorite 프로퍼티 관찰.
     * 1.현재 viewPager's page 내 photoTicket의 favorite 값에 따라 bottomNavi의 menuItem "즐겨찾기" 의 Icon을 변경.
     * */
    protected open fun observeFavorite(viewModel:SolaroidFrameViewModel ,binding: FragmentSolaroidFrameBinding) {
        viewModel.favorite.observe(viewLifecycleOwner, Observer { favor ->
            favor?.let {
                Log.d("FrameFragment", "viewModel.favorite.observe  : ${favor}")
                //getItem은 오류 findItem이랑 다른듯.!
                val menuItem: MenuItem =
                    binding.fragmentFrameBottomNavi.menu.findItem(R.id.favorite)
                menuItem.setIcon(if (!it) R.drawable.ic_favorite_false else R.drawable.ic_favorite_true)
                Log.d("FrameFragment", "Success")
            }
        })
    }

    /**
     * viewModel의 popUpMenu 프로퍼티를 관찰.
     * 현재 기능 : popUpMenu 값이 True로 바뀌었을 시, popupShow() 호출 뒤, viewModel의 doneFilterPopupMenu() 호출
     * */
    protected open fun observePopupMenu(viewModel:SolaroidFrameViewModel, binding: FragmentSolaroidFrameBinding) {
        viewModel.popUpMenu.observe(viewLifecycleOwner, Observer {
            if (it) {
                popupShow(binding.popupMenuFilter)
                viewModel.doneFilterPopupMenu()
            }
        })
    }

    /**
     * viewModel의 photoTicket 프로퍼티를 관찰.
     * 현재 기능 : photoTicket 데이터 변경 시, viewModel의 setCurrentFavorite 함수 호출 후, 현재 photoTicket의 favorite 값 인자로 넘겨줌.
     */
    protected open fun observePhotoTicket(viewModel:SolaroidFrameViewModel) {
        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("FrameFragment", "viewModel.photoTicket.observe  : ${it.id}")
                viewModel.setCurrentFavorite(it.favorite)
            }

        })
    }


    /**
     * adapter viewHolder 의 itemList 를 click 시, SolaroidDetailFragment 탐색.
     */
    protected open fun navigateToDetailFragment(viewModel:SolaroidFrameViewModel) {
        viewModel.navigateToDetailFrag.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    SolaroidFrameFragmentDirections.actionFrameFragmentToDetailFragment(it)
                )
                viewModel.doneNaviToDetailFrag()
            }
        })
    }

    protected open fun popupShow(view: View) {
        val popUp = PopupMenu(this.activity, view)
        popUp.setOnMenuItemClickListener(this@SolaroidFrameFragment)
        popUp.menuInflater.inflate(R.menu.fragment_frame_popup_menu, popUp.menu)
        popUp.show()
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        return when (p0?.itemId) {
            R.id.filter_favorite -> {
                viewModel.sortByFilter(PhotoTicketFilter.FAVORITE)
                //Toast.makeText(this.activity, "즐겨찾기", Toast.LENGTH_SHORT).show()
                viewModel.naviToFrame(true)
                true
            }
            else -> false
        }
    }


}


/**
 * Frame 프래그먼트에서 viewPager의 item들을 즐겨찾기가 활성화 된 item만 list에 display하기 위한 프래그먼트
 */
class SolaroidFrameFavoriteFragment : SolaroidFrameFragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var favoriteViewModel: SolaroidFrameViewModel
    private lateinit var favoriteViewModelFactory: SolaroidFrameViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSolaroidFrameBinding>(
            inflater,
            R.layout.fragment_solaroid_frame,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = SolaroidDatabase.getInstance(application)


        favoriteViewModelFactory = SolaroidFrameViewModelFactory(dataSource.photoTicketDao, application)
        favoriteViewModel = ViewModelProvider(
            this.requireActivity(),
            favoriteViewModelFactory
        )[SolaroidFrameViewModel::class.java]


        val adapter = SolaroidFrameAdapter(OnClickListener { photoTicketKey ->
            favoriteViewModel.naviToDetail(photoTicketKey)
        })

        binding.viewModel = favoriteViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewpager.adapter = adapter

        favoriteViewModel .navigateToFrameFrag.observe(viewLifecycleOwner, Observer{
            if(it) {
                findNavController().navigate(
                    SolaroidFrameFavoriteFragmentDirections.actionFrameFavoriteFragmentToFrameFragment()
                )
                favoriteViewModel .doneNaviToFrameFrag()
            }
        })


        //adapter click event 설정
        navigateToDetailFragment(favoriteViewModel )

        //현재 페이지의 포토티켓 즐겨찾기 여부를 확인.
        observePhotoTicket(favoriteViewModel )

        //popUp Menu 호출
        observePopupMenu(favoriteViewModel ,binding)

        //얘도정리
        observeFavorite(favoriteViewModel ,binding)


        //02.21 얘도정리.
        registerOnPageChangeCallback(favoriteViewModel,binding, adapter)


        //02.21 이거정리.
        setOnItemSelectedListener(favoriteViewModel,binding)


        return binding.root
    }



    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        return when (p0?.itemId) {
            R.id.filter_lately -> {
                favoriteViewModel.sortByFilter(PhotoTicketFilter.LATELY)
                //Toast.makeText(this.activity, "즐겨찾기", Toast.LENGTH_SHORT).show()
                favoriteViewModel.naviToFrame(true)
                true
            }
            else -> false
        }
    }
}