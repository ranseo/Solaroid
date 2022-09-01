package com.ranseo.solaroid.adapter


import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ranseo.solaroid.R
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.databinding.ListItemSolaroidGalleryBinding
import com.ranseo.solaroid.databinding.ListItemSolaroidGalleryLongClickBinding
import java.lang.ClassCastException


const val VIEW_TYPE_NORMAL_GALLERY = 0
const val VIEW_TYPE_LONGCLICK_GALLERY = 1
const val VIEW_TYPE_GALLERY_EMPTY_TITLE = 2


class SolaroidGalleryAdapter(
    val clickListener: OnGalleryClickListener,
    val onLongClickListener: OnGalleryLongClickListener,
    val application: Application
) :
    ListAdapter<GalleryListDataItem, RecyclerView.ViewHolder>(GalleryDataItemCallback()) {


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GalleryListDataItem.NormalGalleryDataItem -> VIEW_TYPE_NORMAL_GALLERY
            is GalleryListDataItem.LongClickGalleryDataItem -> VIEW_TYPE_LONGCLICK_GALLERY
            is GalleryListDataItem.GalleryEmptyDataItem -> VIEW_TYPE_GALLERY_EMPTY_TITLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL_GALLERY -> NormalViewHolder.from(parent)
            VIEW_TYPE_LONGCLICK_GALLERY -> LongClickViewHolder.from(parent)
            VIEW_TYPE_GALLERY_EMPTY_TITLE -> NormalGalleryEmptyViewHolder.from(parent)
            else -> throw ClassCastException("UNKNOWN_VIEW_TYPE_${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NormalViewHolder -> {
                val item = getItem(position) as GalleryListDataItem.NormalGalleryDataItem
                holder.bind(item.photoTicket, clickListener, onLongClickListener)
            }
            is LongClickViewHolder -> {
                val item = getItem(position) as GalleryListDataItem.LongClickGalleryDataItem
                holder.bind(item.photoTicket, onLongClickListener, application)
            }
        }
    }


    class NormalViewHolder(val binding: ListItemSolaroidGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: PhotoTicket,
            clickListener: OnGalleryClickListener,
            longClickListener: OnGalleryLongClickListener
        ) {
            this.binding.photoTicket = item
            this.binding.clickListener = clickListener
            binding.longClickListener = longClickListener
        }

        companion object {
            fun from(parent: ViewGroup): NormalViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidGalleryBinding.inflate(layoutInflater, parent, false)
                return NormalViewHolder(binding)
            }
        }
    }

    class LongClickViewHolder(val binding: ListItemSolaroidGalleryLongClickBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: PhotoTicket,
            onLongClickListener: OnGalleryLongClickListener,
            application: Application
        ) {
            binding.photoTicket = item
            binding.clickListener = onLongClickListener
            binding.flag = false
            val lambda: (view: View) -> Unit = {
                setFlag(application)
            }

            binding.photoLayout.setOnClickListener{
                onLongClickListener.onClick(item)
                setFlag(application)

            }
            binding.btnCheck.setOnClickListener(lambda)
            binding.btnEmpty.setOnClickListener(lambda)
        }

        fun setFlag(application: Application) {
            try {
                binding.flag = !binding.flag
                if (binding.flag) {
                    binding.photoLayout.background =
                        application!!.getDrawable(R.drawable.border_line_yellow)
                } else {
                    binding.photoLayout.background =
                        application!!.getDrawable(R.color.white)
                }
            } catch (error: Exception) {

            }
        }

        companion object {
            fun from(parent: ViewGroup): LongClickViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ListItemSolaroidGalleryLongClickBinding.inflate(layoutInflater, parent, false)
                return LongClickViewHolder(binding)
            }
        }
    }

    class NormalGalleryEmptyViewHolder(view:View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent:ViewGroup) : NormalGalleryEmptyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_item_gallery_empty, parent, false)
                return NormalGalleryEmptyViewHolder(view)
            }
        }
    }


}

class GalleryDataItemCallback() : DiffUtil.ItemCallback<GalleryListDataItem>() {
    override fun areItemsTheSame(
        oldItem: GalleryListDataItem,
        newItem: GalleryListDataItem
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: GalleryListDataItem,
        newItem: GalleryListDataItem
    ): Boolean = oldItem.id == newItem.id
}

sealed class GalleryListDataItem() {
    abstract val id: String

    class NormalGalleryDataItem(val photoTicket: PhotoTicket) : GalleryListDataItem() {
        override val id: String
            get() = photoTicket.id
    }

    class LongClickGalleryDataItem(val photoTicket: PhotoTicket) : GalleryListDataItem() {
        override val id: String
            get() = photoTicket.id
    }

    object GalleryEmptyDataItem : GalleryListDataItem() {
        override val id : String = "GalleryEmpty"
    }
}


