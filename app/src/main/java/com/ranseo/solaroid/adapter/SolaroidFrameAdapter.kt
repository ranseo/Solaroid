package com.ranseo.solaroid.adapter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ranseo.solaroid.custom.view.AlbumThumbnailView
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.databinding.ListItemSolaroidFrameBinding
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep

class SolaroidFrameAdapter(val onFrameLongClickListener: OnFrameLongClickListener, val onFrameShareListener:OnFrameShareListener) :
    ListAdapter<PhotoTicket, SolaroidFrameAdapter.PhotoViewHolder>(PhotoTicketDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onFrameLongClickListener, onFrameShareListener)

        //holder.binding.executePendingBindings()
    }


    class PhotoViewHolder(val binding: ListItemSolaroidFrameBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(item: PhotoTicket?, onLongClickListener:OnFrameLongClickListener, onShareListener:OnFrameShareListener) {
            this.binding.photoTicket = item
            this.binding.onLongClickListener = onLongClickListener


            binding.frontLayout.setOnClickListener {
                val toggle = binding.imageSpin
                binding.imageSpin = !toggle
            }

            binding.backLayout.setOnClickListener {
                val toggle = binding.imageSpin
                binding.imageSpin = !toggle
            }


            binding.frontLayout.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
              //  Log.i("포토뷰홀더","frontLayout.addOnLayoutChangeListener ")
                val frontImage = (view as ConstraintLayout).getBitmapFromView()
                val pos = absoluteAdapterPosition
                onShareListener.onShareFront(frontImage, pos)
            }


            binding.backLayout.addOnLayoutChangeListener {view, _, _, _, _, _, _, _, _ ->
              //  Log.i("포토뷰홀더","backLayout.addOnLayoutChangeListener ")
                val backImage = (view as ConstraintLayout).getBitmapFromView()
                val pos = absoluteAdapterPosition
                onShareListener.onShareBack(backImage, pos)
            }
        }

        companion object {
            fun from(parent: ViewGroup): PhotoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSolaroidFrameBinding.inflate(layoutInflater, parent, false)
                return PhotoViewHolder(binding)
            }
        }

        fun ConstraintLayout.getBitmapFromView(): Bitmap {
            val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            this.draw(canvas)
            return bitmap
        }
    }


}

