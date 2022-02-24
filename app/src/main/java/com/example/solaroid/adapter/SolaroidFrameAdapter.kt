package com.example.solaroid.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.databinding.ListItemSolaroidFrameBinding

class SolaroidFrameAdapter(val clickListener: OnClickListener) :
    ListAdapter<PhotoTicket, SolaroidFrameAdapter.PhotoViewHolder>(PhotoTicketDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
        //holder.binding.executePendingBindings()
    }

    fun getPhotoTicket(position: Int): PhotoTicket? {
        if(itemCount==0) return null
        val photoTicket = getItem(position)
        Log.d("favoriteFrame", "getPhotoTicket: ${photoTicket?.id}")
        return photoTicket
    }


    class PhotoViewHolder(val binding: ListItemSolaroidFrameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PhotoTicket?, clickListener: OnClickListener) {
            this.binding.photoTicket = item
            this.binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): PhotoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidFrameBinding.inflate(layoutInflater, parent, false)
                return PhotoViewHolder(binding)
            }
        }
    }

}

