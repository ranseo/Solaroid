package com.example.solaroid.solaroidframe

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.solaroid.R
import com.example.solaroid.adapter.SolaroidFrameAdapter
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.domain.Profile
import com.example.solaroid.friend.adapter.FriendListAdatper

@BindingAdapter("submitList")
fun bindViewPager(viewPager: ViewPager2, photoTickets: List<PhotoTicket>?) {
    val adapter = viewPager.adapter as SolaroidFrameAdapter
    photoTickets?.let {
        Log.i("바인딩어댑터","포토티켓값 : ${it}")
        adapter.submitList(it)
    }
}


@BindingAdapter("submitList")
fun bindRecycler(recyclerView: RecyclerView, photoTickets: List<PhotoTicket>?) {
    val adapter = recyclerView.adapter as SolaroidGalleryAdapter
    photoTickets?.let {
        Log.i("바인딩어댑터","포토티켓값 : ${it}")
        adapter.submitList(it)
    }
}

@JvmName("bindRecycler1")
@BindingAdapter("submitList")
fun bindRecycler(recyclerView: RecyclerView, profiles: List<Profile>?) {
    val adapter = recyclerView.adapter as FriendListAdatper
    profiles?.let {
        Log.i("바인딩어댑터","포토티켓값 : ${it}")
        adapter.submitList(it)
    }
}


@BindingAdapter("setImage")
fun bindImage(imageView: ImageView, imgUri: String?) {
    imgUri?.let {

        val uri = imgUri.toUri()
        Glide.with(imageView.context)
            .load(uri).apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(imageView)
    }
}

@BindingAdapter("setImage")
fun bindImage (imageView: ImageView, imgUri: Uri?) {
    imgUri?.let {

        Glide.with(imageView.context)
            .load(it).apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(imageView)
    }
}
