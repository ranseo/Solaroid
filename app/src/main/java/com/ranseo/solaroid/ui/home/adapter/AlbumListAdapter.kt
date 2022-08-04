package com.ranseo.solaroid.ui.album.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ranseo.solaroid.R
import com.ranseo.solaroid.models.domain.Album
import com.ranseo.solaroid.models.domain.RequestAlbum
import com.ranseo.solaroid.databinding.ListItemAlbumBinding
import com.ranseo.solaroid.databinding.ListItemAlbumRequestBinding
import com.ranseo.solaroid.ui.home.adapter.AlbumListClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private val VIEW_TYPE_NORMAL_ALBUM = 0
private val VIEW_TYPE_REQUEST_ALBUM = 1
private val VIEW_TYPE_NORMAL_ALBUM_EMPTY = 2
private val VIEW_TYPE_NORMAL_REQUEST_EMPTY = 3

class AlbumListAdapter(val albumListClickListener: AlbumListClickListener) :
    ListAdapter<AlbumListDataItem, RecyclerView.ViewHolder>(AlbumListItemCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AlbumListDataItem.NormalAlbumDataItem -> VIEW_TYPE_NORMAL_ALBUM
            is AlbumListDataItem.RequestAlbumDataItem -> VIEW_TYPE_REQUEST_ALBUM
            is AlbumListDataItem.NormalAlbumEmpty -> VIEW_TYPE_NORMAL_ALBUM_EMPTY
            is AlbumListDataItem.RequestAlbumEmpty-> VIEW_TYPE_NORMAL_REQUEST_EMPTY
            else -> throw IllegalArgumentException("UNKNOWN_CLASS")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL_ALBUM -> NormalAlbumViewHolder.from(parent)
            VIEW_TYPE_REQUEST_ALBUM -> RequestAlbumViewHolder.from(parent)
            VIEW_TYPE_NORMAL_ALBUM_EMPTY ->NormalAlbumEmptyViewHolder.from(parent)
            VIEW_TYPE_NORMAL_REQUEST_EMPTY -> RequestAlbumEmptyViewHolder.from(parent)
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

    class RequestAlbumViewHolder(val binding: ListItemAlbumRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RequestAlbum, onClickListener: AlbumListClickListener) {
            binding.album = item
            binding.ivAlbum.setImageBitmap(item.thumbnail)
            binding.onClickListener = onClickListener
        }

        companion object {
            private val TAG = "RequestAlbumViewHolder"
            fun from(parent: ViewGroup): RequestAlbumViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAlbumRequestBinding.inflate(layoutInflater, parent, false)
                return RequestAlbumViewHolder(binding)
            }
        }

    }

    class NormalAlbumViewHolder(val binding: ListItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Album, onClickListener: AlbumListClickListener) {
            Log.i(TAG,"NormalAlbumViewHolder : ${item.id}, ${item.thumbnail}, ${item.name}")
            binding.album = item
            binding.ivAlbum.setImageBitmap(item.thumbnail)
            binding.tvAlbum.text = if(item.name.length >= 30) item.name.substring(0..25)+".." else item.name
            binding.onClickListener = onClickListener

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

    class NormalAlbumEmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent:ViewGroup) : NormalAlbumEmptyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_item_album_empty, parent, false)
                return NormalAlbumEmptyViewHolder(view)
            }
        }
    }

    class RequestAlbumEmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent:ViewGroup) : RequestAlbumEmptyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_item_request_album_empty, parent, false)
                return RequestAlbumEmptyViewHolder(view)
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

    object NormalAlbumEmpty : AlbumListDataItem() {
        override val id : String = "NormalAlbumEmpty"
    }

    object RequestAlbumEmpty : AlbumListDataItem() {
        override val id : String = "RequestAlbumEmpty"
    }
}
