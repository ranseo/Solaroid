package com.example.solaroid.adapter

import android.net.Uri
import com.example.solaroid.domain.PhotoTicket
import com.example.solaroid.domain.MediaStoreData

class OnClickListener(val clickListener: (photoTicketKey:String)->Unit) {
    fun onClick(photoTicket: PhotoTicket) {
        clickListener(photoTicket.id)
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

