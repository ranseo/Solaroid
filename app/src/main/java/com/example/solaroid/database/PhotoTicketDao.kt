package com.example.solaroid.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabasePhotoTicketDao {
    @Insert
    suspend fun insert(DatabasePhotoTicket:DatabasePhotoTicket)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(DatabasePhotoTickets: List<DatabasePhotoTicket>)

    @Update
    suspend fun update(DatabasePhotoTicket: DatabasePhotoTicket)

    @Query("DELETE FROM photo_ticket_table WHERE id == :key")
    suspend fun delete(key:Long)

    //최신순 정렬
    @Query("SELECT * FROM photo_ticket_table ORDER BY id DESC")
    fun getAllDatabasePhotoTicket() : LiveData<List<DatabasePhotoTicket>?>

    //즐겨찾기 표시된 포토티켓만 정렬
    @Query("SELECT * FROM photo_ticket_table WHERE favorite == :favorite ORDER BY id DESC")
    fun getFavoriteDatabasePhotoTicket(favorite:Boolean) : LiveData<List<DatabasePhotoTicket>?>

    //제일 최근에 만든 포토티켓
    @Query("SELECT * FROM photo_ticket_table ORDER BY id DESC LIMIT 1")
    suspend fun getLatestTicket() : DatabasePhotoTicket?

    //전부 삭제.
    @Query("DELETE FROM photo_ticket_table")
    suspend fun clear()

    //유저가 원하는 포토티켓
    @Query("SELECT * FROM photo_ticket_table WHERE :key == id")
    suspend fun getDatabasePhotoTicket(key:Long) : DatabasePhotoTicket




}