package com.ranseo.solaroid.models.domain

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class MediaStoreData(
    val id:Long,
    val displayName:String,
    val date: String,
    val contentUri: Uri
) {
    companion object {
        fun itemCallback() = object : DiffUtil.ItemCallback<MediaStoreData>() {
            override fun areItemsTheSame(oldItem: MediaStoreData, newItem: MediaStoreData): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MediaStoreData, newItem: MediaStoreData): Boolean = oldItem == newItem

        }
    }
}