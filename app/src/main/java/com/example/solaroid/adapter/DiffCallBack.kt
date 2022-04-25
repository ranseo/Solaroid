package com.example.solaroid.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.solaroid.domain.PhotoTicket

class PhotoTicketDiffCallback : DiffUtil.ItemCallback<PhotoTicket>() {
    override fun areItemsTheSame(oldItem: PhotoTicket, newItem: PhotoTicket): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PhotoTicket, newItem: PhotoTicket): Boolean {
        return oldItem == newItem
    }
}