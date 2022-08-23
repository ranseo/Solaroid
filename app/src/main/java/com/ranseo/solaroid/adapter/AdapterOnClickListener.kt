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

class OnFrameShareListener(val shareFrontListener: (front: Bitmap)->Unit, val shareBackListener: (back: Bitmap)->Unit) {
    fun onShareFront(front: Bitmap) {
        shareFrontListener(front)
    }

    fun onShareBack(back: Bitmap) {
        shareBackListener(back)
    }
}

class OnChoiceClickListener(val clickListener: (imageUri: Uri)->Unit) {
    fun onClick(image: MediaStoreData) {
        clickListener(image.contentUri)
    }
}

