package com.example.solaroid.solaroidframe

import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.PhotoTicket

@BindingAdapter("submitList")
fun bindViewPager(viewPager : ViewPager2, photoTickets:List<PhotoTicket>) {
    val adapter = viewPager.adapter as SolaroidFrameAdapter
    adapter.submitList(photoTickets)
}
