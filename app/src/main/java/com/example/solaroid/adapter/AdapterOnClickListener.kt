package com.example.solaroid.adapter

import com.example.solaroid.database.PhotoTicket

class OnClickListener(val clickListener: (photoTicketKey:Long)->Unit) {
    fun onClick(photoTicket: PhotoTicket) {
        clickListener(photoTicket.id)
    }
}
