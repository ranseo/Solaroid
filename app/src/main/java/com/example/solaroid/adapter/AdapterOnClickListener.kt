package com.example.solaroid.adapter

import android.net.Uri
import com.example.solaroid.database.PhotoTicket
import com.example.solaroid.solaroidadd.MediaStoreData

class OnClickListener(val clickListener: (photoTicketKey:Long)->Unit) {
    fun onClick(photoTicket: PhotoTicket) {
        clickListener(photoTicket.id)
    }
}

class OnFrameLongClickListener(val clickListener: (photoTicketKey:Long)->Unit) {
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

