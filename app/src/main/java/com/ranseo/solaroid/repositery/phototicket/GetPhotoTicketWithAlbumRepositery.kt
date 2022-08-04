package com.ranseo.solaroid.repositery.phototicket

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ranseo.solaroid.models.domain.PhotoTicket
import com.ranseo.solaroid.models.room.asDomainModel
import com.ranseo.solaroid.room.DatabasePhotoTicketDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener

class GetPhotoTicketWithAlbumRepositery(
    private val dataSource: DatabasePhotoTicketDao,
    private val fbAuth: FirebaseAuth,
    private val albumId: String
) {

    private var listener : ValueEventListener? = null
    val user = fbAuth.currentUser!!.email!!

    /**
     * 포토티켓의 날짜기준 내림차순 정렬.
     * */
    val photoTicketsOrderByDesc: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllPhotoTicketWithUserAndAlbumIdDesc(albumId, user)) {
            it?.asDomainModel()
        }

    /**
     * 포토티켓의 날짜기준 오름차순 정렬.
     * */
    val photoTicketsOrderByAsc: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllPhotoTicketWithUserAndAlbumIdAsc(albumId, user)) {
            it?.asDomainModel()
        }

    /**
     * 포토티켓의 즐겨찾기만 정렬
     * */
    val photoTicketsOrderByFavorite: LiveData<List<PhotoTicket>> =
        Transformations.map(dataSource.getAllPhotoTicketWithUserAndAlbumIdFavorite(albumId ,user, true)) {
            it?.asDomainModel()
        }



}