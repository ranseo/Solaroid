package com.example.solaroid.solaroidframe

import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.ui.NavigationUI
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.R
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.databinding.FragmentSolaroidFrameBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class SolaroidFrameFragmentFilter : Fragment() {

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
                val photoTicket = adapter.getPhotoTicket(position)
                photoTicket?.let {
                    viewModel.setCurrentPhotoTicket(photoTicket)
                }
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
                Log.d("FrameFragment", "viewModel.photoTicket.observe  : ${it.id}")
                viewModel.setCurrentFavorite(it.favorite)
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
     * bottomNavigation의 item Selection별 기능 구현 함수.
     * 1.선택한 item이 favorite 일 때, viewModel의 favorite 값의 !favorite 값을 viewModel의 togglePhotoTicketFavorite() 호출하여 인자로 넘겨줌.
     * */
    protected open fun setOnItemSelectedListener(
        viewModel: SolaroidFrameViewModel,
        botNavi: BottomNavigationView
    ) {}

}
