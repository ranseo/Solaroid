package com.example.solaroid.ui.album.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.models.domain.Album
import com.example.solaroid.models.domain.RequestAlbum
import com.example.solaroid.databinding.ListItemAlbumBinding
import com.example.solaroid.databinding.ListItemRequestAlbumBinding
import com.example.solaroid.ui.home.adapter.AlbumListClickListener
import com.example.solaroid.utils.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val VIEW_TYPE_NORMAL_ALBUM = 0
private val VIEW_TYPE_REQUEST_ALBUM = 1

class AlbumListAdapter(val albumListClickListener: AlbumListClickListener) :
    ListAdapter<AlbumListDataItem, RecyclerView.ViewHolder>(AlbumListItemCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitList(normal: List<Album>? = null, request: List<RequestAlbum>? = null) {
        adapterScope.launch {

            val list =
                if (normal.isNullOrEmpty()) request?.map { AlbumListDataItem.RequestAlbumDataItem(it) }
                else if (request.isNullOrEmpty()) normal.map { AlbumListDataItem.NormalAlbumDataItem(it) }
                else {
                    normal.map {
                        AlbumListDataItem.NormalAlbumDataItem(it)
                    } + request.map { AlbumListDataItem.RequestAlbumDataItem(it) }
                }


            withContext(Dispatchers.Main) {
                submitList(list)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AlbumListDataItem.NormalAlbumDataItem -> VIEW_TYPE_NORMAL_ALBUM
            is AlbumListDataItem.RequestAlbumDataItem -> VIEW_TYPE_REQUEST_ALBUM
            else -> throw IllegalArgumentException("UNKNOWN_CLASS")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL_ALBUM -> NormalAlbumViewHolder.from(parent)
            VIEW_TYPE_REQUEST_ALBUM -> RequestAlbumViewHolder.from(parent)
            else -> throw IllegalArgumentException("UNKNOWN_VIEWTYPE")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NormalAlbumViewHolder -> {
                val item = getItem(position) as AlbumListDataItem.NormalAlbumDataItem
                holder.bind(item.album, albumListClickListener)
            }
            is RequestAlbumViewHolder -> {
                val item = getItem(position) as AlbumListDataItem.RequestAlbumDataItem
                holder.bind(item.album, albumListClickListener)
            }
        }
    }

    class RequestAlbumViewHolder(val binding: ListItemRequestAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RequestAlbum, onClickListener: AlbumListClickListener) {

            binding.album = item
        }

        companion object {
            fun from(parent: ViewGroup): RequestAlbumViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemRequestAlbumBinding.inflate(layoutInflater, parent, false)
                return RequestAlbumViewHolder(binding)
            }
        }

    }

    class NormalAlbumViewHolder(val binding: ListItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Album, onClickListener: AlbumListClickListener) {
            binding.album = item
            binding.ivAlbum.setImageBitmap(item.thumbnail)
        }

        companion object {
            private val TAG = "NormalAlbumViewHolder"
            fun from(parent: ViewGroup): NormalAlbumViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAlbumBinding.inflate(layoutInflater, parent, false)
                return NormalAlbumViewHolder(binding)
            }
        }
    }
}


class AlbumListItemCallback : DiffUtil.ItemCallback<AlbumListDataItem>() {
    override fun areItemsTheSame(oldItem: AlbumListDataItem, newItem: AlbumListDataItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: AlbumListDataItem,
        newItem: AlbumListDataItem
    ) = oldItem == newItem
}

sealed class AlbumListDataItem() {
    abstract val id: String

    class NormalAlbumDataItem(val album: Album) : AlbumListDataItem() {
        override val id: String = album.id
    }

    class RequestAlbumDataItem(val album: RequestAlbum) : AlbumListDataItem() {
        override val id: String = album.id
    }
}
