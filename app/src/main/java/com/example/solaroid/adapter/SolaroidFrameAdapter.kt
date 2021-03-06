package com.example.solaroid.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.models.domain.PhotoTicket
import com.example.solaroid.databinding.ListItemSolaroidFrameBinding

class SolaroidFrameAdapter(val onFrameLongClickListener: OnFrameLongClickListener) :
    ListAdapter<PhotoTicket, SolaroidFrameAdapter.PhotoViewHolder>(PhotoTicketDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onFrameLongClickListener)

        //holder.binding.executePendingBindings()
    }

    fun getPhotoTicket(position: Int): PhotoTicket? {
        if (itemCount == 0) return null
        val photoTicket = getItem(position)
        Log.d("favoriteFrame", "getPhotoTicket: ${photoTicket?.id}")
        return photoTicket
    }


    class PhotoViewHolder(val binding: ListItemSolaroidFrameBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(item: PhotoTicket?, onLongClickListener:OnFrameLongClickListener) {
            this.binding.photoTicket = item
            this.binding.onLongClickListener = onLongClickListener


            binding.frontLayout.setOnClickListener {
                val toggle = binding.imageSpin
                binding.imageSpin = !toggle
            }

            binding.backLayout.setOnClickListener {
                val toggle = binding.imageSpin
                binding.imageSpin = !toggle
            }
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

