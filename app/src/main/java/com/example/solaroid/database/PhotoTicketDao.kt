package com.example.solaroid.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabasePhotoTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(DatabasePhotoTicket:DatabasePhotoTicket)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(DatabaseProfile: DatabaseProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(DatabasePhotoTickets: List<DatabasePhotoTicket>)

    @Update
    suspend fun update(DatabasePhotoTicket: DatabasePhotoTicket)

    @Query("DELETE FROM photo_ticket_table WHERE photo_ticket_key == :key")
    suspend fun delete(key:String)

    //최신순 정렬
    @Query("SELECT * FROM photo_ticket_table ORDER BY photo_ticket_date DESC")
    fun getAllDatabasePhotoTicket() : LiveData<List<DatabasePhotoTicket>?>

    //즐겨찾기 표시된 포토티켓만 정렬
    @Query("SELECT * FROM photo_ticket_table WHERE favorite == :favorite ORDER BY  photo_ticket_date DESC")
    fun getFavoriteDatabasePhotoTicket(favorite:Boolean) : LiveData<List<DatabasePhotoTicket>?>

    @Query("SELECT * FROM photo_ticket_table WHERE ")
    //제일 최근에 만든 포토티켓
    @Query("SELECT * FROM photo_ticket_table ORDER BY  photo_ticket_date DESC LIMIT 1")
    suspend fun getLatestTicket() : DatabasePhotoTicket?

    //전부 삭제.
    @Query("DELETE FROM photo_ticket_table")
    suspend fun clear()

    //유저가 원하는 포토티켓
    @Query("SELECT * FROM photo_ticket_table WHERE :key == photo_ticket_key")
    suspend fun getDatabasePhotoTicket(key:String) : DatabasePhotoTicket

}