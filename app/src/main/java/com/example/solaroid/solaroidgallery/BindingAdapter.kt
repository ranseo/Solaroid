package com.example.solaroid.solaroidgallery

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.database.PhotoTicket


@BindingAdapter("submitList")
fun bindRec(recyclerView: RecyclerView, photoTickets:List<PhotoTicket>?) {
    val adapter = recyclerView.adapter as SolaroidGalleryAdapter
    photoTickets?.let{
        adapter.submitList(it)
    }
}