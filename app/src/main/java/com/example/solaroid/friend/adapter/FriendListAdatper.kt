package com.example.solaroid.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.databinding.ListItemFriendDispatchBinding
import com.example.solaroid.databinding.ListItemFriendReceptionBinding
import com.example.solaroid.databinding.ListItemSolaroidFriendBinding
import com.example.solaroid.domain.Profile
import com.example.solaroid.friend.fragment.add.dispatch.DispatchProfile
import com.example.solaroid.friend.fragment.add.reception.ReceptionProfile
import java.lang.ClassCastException

private val VIEW_TYPE_NORMAL_PROFILE = 0
private val VIEW_TYPE_RECEPTION_PROFILE = 1
private val VIEW_TYPE_DISPATCH_PROFILE = 2

class FriendListAdatper : ListAdapter<FriendListDataItem,RecyclerView.ViewHolder>(FriendListDataItemCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is FriendListDataItem.NormalProfileDataItem -> VIEW_TYPE_NORMAL_PROFILE
            is FriendListDataItem.ReceptionProfileDataItem -> VIEW_TYPE_RECEPTION_PROFILE
            is FriendListDataItem.DispatchProfileDataItem -> VIEW_TYPE_DISPATCH_PROFILE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendListViewHolder {
        return when(viewType) {
            VIEW_TYPE_NORMAL_PROFILE -> FriendListViewHolder.from(parent)
            VIEW_TYPE_RECEPTION_PROFILE -> FriendReceptionViewHolder.from(parent)
            VIEW_TYPE_DISPATCH_PROFILE -> FriendDispatchViewHolder.from(parent)
            else -> throw ClassCastException("UNKNOWN_VIEWTYPE_${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is FriendListViewHolder -> {
                val item = getItem(position) as FriendListDataItem.NormalProfileDataItem
                holder.bind(item.profiles)
            }
            is FriendReceptionViewHolder -> {
                val item = getItem(position) as FriendListDataItem.ReceptionProfileDataItem
                holder.bind(item.profiles)
            }
            is FriendDispatchViewHolder -> {
                val item = getItem(position) as FriendListDataItem.DispatchProfileDataItem
                holder.bind(item.profiles)
            }
        }
    }

    class FriendListViewHolder(private val binding : ListItemSolaroidFriendBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item:Profile) {
            binding.profile = item
        }

        companion object {
            fun from(parent: ViewGroup) : FriendListViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidFriendBinding.inflate(layoutInflater, parent, false)
                return FriendListViewHolder(binding)
            }
        }
    }

    class FriendReceptionViewHolder(private val binding : ListItemFriendReceptionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item:ReceptionProfile) {
            binding.profile = item.profile
        }

        companion object {
            fun from(parent: ViewGroup) : FriendListViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidFriendBinding.inflate(layoutInflater, parent, false)
                return FriendListViewHolder(binding)
            }
        }
    }

    class FriendDispatchViewHolder(private val binding : ListItemFriendDispatchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item:DispatchProfile) {
            binding.profile = item.profile
        }

        companion object {
            fun from(parent: ViewGroup) : FriendListViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidFriendBinding.inflate(layoutInflater, parent, false)
                return FriendListViewHolder(binding)
            }
        }
    }
}

class FriendListDataItemCallback() : DiffUtil.ItemCallback<FriendListDataItem>() {
    override fun areItemsTheSame(
        oldItem: FriendListDataItem,
        newItem: FriendListDataItem
    ): Boolean = oldItem == newItem


    override fun areContentsTheSame(
        oldItem: FriendListDataItem,
        newItem: FriendListDataItem
    ): Boolean = oldItem == newItem
}


sealed class FriendListDataItem() {
    class NormalProfileDataItem(val profiles: Profile) : FriendListDataItem() {

    }

    class ReceptionProfileDataItem(val profiles: ReceptionProfile) : FriendListDataItem() {

    }

    class DispatchProfileDataItem(val profiles: DispatchProfile) : FriendListDataItem() {

    }
}
