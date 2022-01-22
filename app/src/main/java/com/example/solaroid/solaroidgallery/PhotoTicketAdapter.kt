package com.example.solaroid.solaroidgallery


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.databinding.ListItemSolaroidPhotoBinding

class PhotoTicketAdapter(val clickListener: OnClickListener) :
    ListAdapter<PhotoTicket, PhotoTicketAdapter.PhotoViewHolder>(PhotoTicketDiffCallback()) {


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

class OnClickListener(val clickListener: (photoTicketKey:Long)->Unit) {
    fun onClick(photoTicket: PhotoTicket) {
        clickListener(photoTicket.id)
    }
}

class PhotoTicketDiffCallback : DiffUtil.ItemCallback<PhotoTicket>() {
    override fun areItemsTheSame(oldItem: PhotoTicket, newItem: PhotoTicket): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PhotoTicket, newItem: PhotoTicket): Boolean {
        return oldItem == newItem
    }
}