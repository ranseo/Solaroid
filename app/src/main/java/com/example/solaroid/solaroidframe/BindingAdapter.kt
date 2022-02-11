package com.example.solaroid.solaroidframe

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.database.PhotoTicket

@BindingAdapter("submitList")
fun bindViewPager(viewPager : ViewPager2, photoTickets:List<PhotoTicket>?) {
    val adapter = viewPager.adapter as SolaroidFrameAdapter
    photoTickets?.let{
        adapter.submitList(photoTickets)
    }

}


@BindingAdapter("setImage")
fun bindImage (imageView: ImageView, imgUri: String?) {
    imgUri?.let {

        val uri = imgUri.toUri()
        Glide.with(imageView.context)
            .load(uri)
            .into(imageView)
    }
}