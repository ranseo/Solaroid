package com.example.solaroid.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.databinding.ListItemSolaroidPhotoBinding
import com.example.solaroid.domain.PhotoTicket

class SolaroidGalleryAdapter(val clickListener: OnClickListener) :
    ListAdapter<PhotoTicket, SolaroidGalleryAdapter.PhotoViewHolder>(PhotoTicketDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
        //holder.binding.executePendingBindings()
    }



    class PhotoViewHolder(val binding: ListItemSolaroidPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PhotoTicket, clickListener: OnClickListener) {
            this.binding.photoTicket = item
            this.binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): PhotoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidPhotoBinding.inflate(layoutInflater, parent, false)
                return PhotoViewHolder(binding)
            }
        }
    }

}


