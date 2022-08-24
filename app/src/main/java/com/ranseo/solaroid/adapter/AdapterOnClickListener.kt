package com.ranseo.solaroid.adapter

import android.graphics.Bitmap
import android.net.Uri

import com.ranseo.solaroid.models.domain.MediaStoreData
import com.ranseo.solaroid.models.domain.PhotoTicket

class OnClickListener(val clickListener: (photoTicket: PhotoTicket)->Unit) {
    fun onClick(photoTicket: PhotoTicket) {
        clickListener(photoTicket)
    }
}

class OnFrameLongClickListener(val clickListener: (photoTicketKey:String)->Unit) {
    fun onClick(photoTicket: PhotoTicket) : Boolean {
        clickListener(photoTicket.id)
        return true
    }
}

class OnFrameShareListener(val shareFrontListener: (front: Bitmap, pos:Int)->Unit, val shareBackListener: (back: Bitmap, pos:Int)->Unit) {
    fun onShareFront(front: Bitmap, pos:Int) {
        shareFrontListener(front, pos)
    }

    fun onShareBack(back: Bitmap, pos:Int) {
        shareBackListener(back, pos)
    }
}

class OnChoiceClickListener(val clickListener: (imageUri: Uri)->Unit) {
    fun onClick(image: MediaStoreData) {
        clickListener(image.contentUri)
    }
}

