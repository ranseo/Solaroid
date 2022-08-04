package com.ranseo.solaroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ranseo.solaroid.databinding.ListItemSolaroidAddChoiceBinding
import com.ranseo.solaroid.models.domain.MediaStoreData

class SolaroidChoiceAdapter(val onClickListener: OnChoiceClickListener) :
    ListAdapter<MediaStoreData, SolaroidChoiceAdapter.ViewHolder>(MediaStoreData.itemCallback()) {

    class ViewHolder(val binding: ListItemSolaroidAddChoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemSolaroidAddChoiceBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
        holder.binding.clickListener = onClickListener
    }

}