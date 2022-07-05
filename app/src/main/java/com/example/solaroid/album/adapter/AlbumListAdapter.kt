package com.example.solaroid.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.data.domain.Album
import com.example.solaroid.data.domain.RequestAlbum
import com.example.solaroid.databinding.ListItemAlbumBinding
import com.example.solaroid.databinding.ListItemRequestAlbumBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private val VIEW_TYPE_NORMAL_ALBUM = 0
private val VIEW_TYPE_REQUEST_ALBUM = 1

class AlbumListAdapter() :
    ListAdapter<AlbumListDataItem, RecyclerView.ViewHolder>(AlbumListItemCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.IO)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AlbumListDataItem.NormalAlbumDataItem -> VIEW_TYPE_NORMAL_ALBUM
            is AlbumListDataItem.RequestAlbumDataItem -> VIEW_TYPE_REQUEST_ALBUM
            else -> throw IllegalArgumentException("UNKNOWN_CLASS")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_NORMAL_ALBUM -> NormalAlbumViewHolder.from(parent)
            VIEW_TYPE_REQUEST_ALBUM -> RequestAlbumViewHolder.from(parent)
            else -> throw IllegalArgumentException("UNKNOWN_VIEWTYPE")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NormalAlbumViewHolder -> {
                val item = getItem(position) as AlbumListDataItem.NormalAlbumDataItem
                holder.bind(item.album)
            }
            is RequestAlbumViewHolder -> {
                val item = getItem(position) as AlbumListDataItem.RequestAlbumDataItem
                holder.bind(item.album)
            }
        }
    }

    class RequestAlbumViewHolder(val binding: ListItemRequestAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RequestAlbum) {

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

        fun bind(item: Album) {

        }

        companion object {
            fun from(parent: ViewGroup): NormalAlbumViewHolder{
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
