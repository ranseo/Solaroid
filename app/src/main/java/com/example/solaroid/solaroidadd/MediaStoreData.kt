package com.example.solaroid.solaroidadd

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.sql.Date

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