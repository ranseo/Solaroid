package com.example.solaroid.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.solaroid.R
import com.example.solaroid.databinding.ListItemSolaroidFriendBinding
import com.example.solaroid.domain.Profile

class FriendListAdatper : ListAdapter<Profile,FriendListAdatper.FriendListViewHolder>(Profile.itemCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendListViewHolder {
        return FriendListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FriendListViewHolder, position: Int) {
        val item = getItem(position)

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

}