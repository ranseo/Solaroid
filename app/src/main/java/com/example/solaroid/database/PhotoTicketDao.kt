package com.example.solaroid.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PhotoTicketDao {
    @Insert
    suspend fun insert(photoTicket:PhotoTicket)

    @Update
    suspend fun update(photoTicket: PhotoTicket)

    //최신순 정렬
    @Query("SELECT * FROM photo_ticket_table ORDER BY id DESC")
    fun getAllPhotoTicket() : LiveData<List<PhotoTicket>>

    //제일 최근에 만든 포토티켓
    @Query("SELECT * FROM photo_ticket_table ORDER BY id DESC LIMIT 1")
    suspend fun getLatestTicket() : PhotoTicket?

    //전부 삭제.
    @Query("DELETE FROM photo_ticket_table")
    suspend fun clear()

    //유저가 원하는 포토티켓
    @Query("SELECT * FROM photo_ticket_table WHERE :key == id")
    suspend fun getPhotoTicket(key:Long) : PhotoTicket

}