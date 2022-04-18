package com.example.solaroid.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoTicketDao {
    @Insert
    suspend fun insert(photoTicket:PhotoTicket)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photoTickets: List<PhotoTicket>)

    @Update
    suspend fun update(photoTicket: PhotoTicket)

    @Query("DELETE FROM photo_ticket_table WHERE id == :key")
    suspend fun delete(key:Long)

    //최신순 정렬
    @Query("SELECT * FROM photo_ticket_table ORDER BY id DESC")
    fun getAllPhotoTicket() : LiveData<List<PhotoTicket>?>

    //즐겨찾기 표시된 포토티켓만 정렬
    @Query("SELECT * FROM photo_ticket_table WHERE favorite == :favorite ORDER BY id DESC")
    fun getFavoritePhotoTicket(favorite:Boolean) : LiveData<List<PhotoTicket>?>

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