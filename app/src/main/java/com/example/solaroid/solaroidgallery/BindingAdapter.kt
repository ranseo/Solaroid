package com.example.solaroid.solaroidgallery

import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.solaroid.adapter.SolaroidGalleryAdapter
import com.example.solaroid.database.PhotoTicket


@BindingAdapter("submitList")
fun bindRec(recyclerView: RecyclerView, photoTickets:List<PhotoTicket>?) {
    val adapter = recyclerView.adapter as SolaroidGalleryAdapter
    photoTickets?.let{
        adapter.submitList(it)
    }
}


