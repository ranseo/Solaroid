package com.example.solaroid.adapter

import android.net.Uri

import com.example.solaroid.models.domain.MediaStoreData
import com.example.solaroid.models.domain.PhotoTicket

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

class OnChoiceClickListener(val clickListener: (imageUri: Uri)->Unit) {
    fun onClick(image: MediaStoreData) {
        clickListener(image.contentUri)
    }
}

