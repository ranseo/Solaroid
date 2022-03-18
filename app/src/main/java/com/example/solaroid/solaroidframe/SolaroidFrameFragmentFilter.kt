package com.example.solaroid.solaroidframe

import android.app.Application
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.SolaroidDatabase
import com.example.solaroid.dialog.ListSetDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class SolaroidFrameFragmentFilter() : Fragment(),
    ListSetDialogFragment.ListSetDialogListener {


    /**
     * binding된 viewPager의 selected page의 PhotoTicket 객체를 추출. -> viewModel의 setCurrentPhotoTicket() 호출하여 인자로 넘겨줌.
     * */
    protected open fun registerOnPageChangeCallback(
        viewModel: SolaroidFrameViewModel,
        viewPager: ViewPager2,
        adapter: SolaroidFrameAdapter
    ) {

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("FrameFragment", "onPageSelected")
                viewModel.setCurrentPosition(position)

            }
        })
    }

    /**
     * viewModel의 photoTicket 프로퍼티를 관찰.
     * 현재 기능 : photoTicket 데이터 변경 시, viewModel의 setCurrentFavorite 함수 호출 후, 현재 photoTicket의 favorite 값 인자로 넘겨줌.
     */
    protected open fun observePhotoTicket(viewModel: SolaroidFrameViewModel) {
        viewModel.photoTicket.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.setCurrentFavorite(it.favorite)
                Log.d("FrameFragment", "observePhotoTicket")
            }
        })
    }


    /**
     * viewModel의 favorite 프로퍼티 관찰.
     * 1.현재 viewPager's page 내 photoTicket의 favorite 값에 따라 bottomNavi의 menuItem "즐겨찾기" 의 Icon을 변경.
     * */
    protected open fun observeFavorite(
        viewModel: SolaroidFrameViewModel,
        botNavi: BottomNavigationView
    ) {
        viewModel.favorite.observe(viewLifecycleOwner, Observer { favor ->
            favor?.let {
                Log.d("FrameFragment", "viewModel.favorite.observe  : ${favor}")
                //getItem은 오류 findItem이랑 다른듯.!
                val menuItem: MenuItem =
                    botNavi.menu.findItem(R.id.favorite)
                menuItem.setIcon(if (!it) R.drawable.ic_favorite_false else R.drawable.ic_favorite_true)
                Log.d("FrameFragment", "Success")
            }
        })
    }


    /**
     * 포토티켓의 삭제 및 [즐겨찾기 프래그먼트]에서의 즐겨찾기 해제로 인해 viewPager의 포토티켓이 사라지는 경우가 발생.
     * 이때 viewPager의 0번째 위치에서 포토티켓이 사라진다면, 뒤에 있던 포토티켓이 0번째 위치로 이동하게 된다.
     * viewPager의 onPagedSelected는 현재 위치가 변경되지 않았기 때문에 감지하지 못함. 따라서 현재 포토티켓을 캐치하지 못하는 문제발생.
     * 이를 방지하기 위해 FrameViewModel에 Position이라는 변수를 만들고, "삭제 및 즐겨찾기 해제"가 발생할 때 현재 위치의
     * 포토티켓을 캐치하여 app이 정상적으로 현 포토티켓 상황을 유저들에게 전달할 수 있도록 함.
     * */
    protected open fun observeCurrentPosition(
        viewModel: SolaroidFrameViewModel,
        adapter: SolaroidFrameAdapter
    ) {
        viewModel.currentPosition.observe(viewLifecycleOwner, Observer { pos ->
            //현재 위치가 0보다 커야한다. (음수가 되는 상황은 발생하지 않음)
            //현재 위치가 adapter내의 전체 아이템의 크기 수보다 작아야 한다. (아이템의 크기를 넘어서 존재할 수 없음)
            pos?.let {
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
    protected open fun refreshCurrentPosition(
        viewModel: SolaroidFrameViewModel,
        viewPager: ViewPager2
    ) {
        viewModel.photoTicketsSize.observe(viewLifecycleOwner, Observer { size ->
            if(size > 0 && size !=null) {

                val position = viewPager.currentItem
                Log.d("FrameFragment", "refreshCurrentPosition photoTicket Id: ${position} : ${size}")
                viewModel.setCurrentPosition(position)
            }
        })

    }

    /**
     * bottomNavigation의 item Selection별 기능 구현 함수.
     * 1.선택한 item이 favorite 일 때, viewModel의 favorite 값의 !favorite 값을 viewModel의 togglePhotoTicketFavorite() 호출하여 인자로 넘겨줌.
     * */
    protected open fun setOnItemSelectedListener(
        viewModel: SolaroidFrameViewModel,
        botNavi: BottomNavigationView,
        viewPager: ViewPager2
    ) {
        botNavi.setOnItemSelectedListener { it ->
            when (it.itemId) {
                R.id.favorite -> {
                    val favorite = viewModel.photoTicket.value?.favorite
                    favorite?.let { favor ->
                        if (favor) viewModel.togglePhotoTicketFavorite(false)
                        else viewModel.togglePhotoTicketFavorite(true)
                    }
                }
                R.id.edit -> {
                    viewModel.navigateToEdit(viewModel.photoTicket.value?.id)
                }
            }
            false
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
        val key = viewModel.photoTicket.value!!.id
        when (position) {
            //delete
            0 -> {
                viewModel.deletePhotoTicket(key)
                dialog.dismiss()
            }
            //수정
            1 -> {
                viewModel.navigateToEdit(key)
                dialog.dismiss()
            }
            //상세정보
            2 -> {
                viewModel.navigateToDetail(key)
                dialog.dismiss()
            }
        }
    }


    protected open fun showListDialog(viewModel: SolaroidFrameViewModel) {
        val newDialogFragment = ListSetDialogFragment(this, viewModel)
        newDialogFragment.show(parentFragmentManager, "ListDialog")
    }

//    protected open fun observeListDialog(
//        viewModel: SolaroidFrameViewModel
//    ) {
//        viewModel.listDialog.observe(viewLifecycleOwner, Observer{
//            if(it) {
//                showListDialog(viewModel)
//            }
//        })
//
//    }


}
