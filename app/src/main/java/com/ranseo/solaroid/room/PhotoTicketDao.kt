package com.ranseo.solaroid.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ranseo.solaroid.models.room.*

@Dao
interface DatabasePhotoTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(DatabasePhotoTicket: DatabasePhotoTicket)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(DatabasePhotoTickets: List<DatabasePhotoTicket>)

    @Update
    suspend fun update(DatabasePhotoTicket: DatabasePhotoTicket)

    @Query("DELETE FROM photo_ticket_table WHERE photo_ticket_key == :key")
    suspend fun delete(key:String)

    //최신순 정렬
    @Query("SELECT * FROM photo_ticket_table ORDER BY photo_ticket_date DESC")
    fun getAllDatabasePhotoTicket() : LiveData<List<DatabasePhotoTicket>?>

    //제일 최근에 만든 포토티켓
    @Query("SELECT * FROM photo_ticket_table ORDER BY  photo_ticket_date DESC LIMIT 1")
    suspend fun getLatestTicket() : DatabasePhotoTicket?

    //전부 삭제.
    @Query("DELETE FROM photo_ticket_table")
    suspend fun clear()

    @Query("DELETE FROM photo_ticket_table WHERE photo_ticket_album_id == :id")
    suspend fun deletePhotoTicketsWithAlbumId(id:String)

    @Query("DELETE FROM photo_ticket_table WHERE photo_ticket_album_id IN (:list)")
    suspend fun deletePhotoTicketsWithAlbumIdList(list:List<String>)

    //유저가 원하는 포토티켓
    @Query("SELECT * FROM photo_ticket_table WHERE :key == photo_ticket_key")
    suspend fun getDatabasePhotoTicket(key:String) : DatabasePhotoTicket

    @Query("SELECT * FROM photo_ticket_table AS pt WHERE pt.photo_ticket_user == :user ORDER BY photo_ticket_date DESC")
    fun getAllPhotoTicketWithUserDesc(user:String) : LiveData<List<DatabasePhotoTicket>?>

    @Query("SELECT * FROM photo_ticket_table AS pt WHERE pt.photo_ticket_user == :user ORDER BY photo_ticket_date ASC")
    fun getAllPhotoTicketWithUserAsc(user:String) : LiveData<List<DatabasePhotoTicket>?>

    @Query("SELECT * FROM photo_ticket_table AS pt WHERE pt.photo_ticket_user == :user AND pt.photo_ticket_favorite == :favorite ORDER BY photo_ticket_date ASC")
    fun getAllPhotoTicketWithUserFavorite(user:String, favorite: Boolean) : LiveData<List<DatabasePhotoTicket>?>

    //Album이 특정된 포토티켓
    @Query("SELECT * FROM photo_ticket_table AS pt WHERE pt.photo_ticket_album_id == :albumId AND pt.photo_ticket_user == :user ORDER BY pt.photo_ticket_date DESC")
    fun getAllPhotoTicketWithUserAndAlbumIdDesc(albumId:String, user:String) : LiveData<List<DatabasePhotoTicket>>

    @Query("SELECT * FROM photo_ticket_table AS pt WHERE pt.photo_ticket_album_id == :albumId AND pt.photo_ticket_user == :user ORDER BY pt.photo_ticket_date ASC")
    fun getAllPhotoTicketWithUserAndAlbumIdAsc(albumId:String, user:String) : LiveData<List<DatabasePhotoTicket>>

    @Query("SELECT * FROM photo_ticket_table AS pt WHERE pt.photo_ticket_album_id == :albumId AND pt.photo_ticket_user == :user AND pt.photo_ticket_favorite == :favorite ORDER BY pt.photo_ticket_date ASC")
    fun getAllPhotoTicketWithUserAndAlbumIdFavorite(albumId:String, user:String, favorite:Boolean) : LiveData<List<DatabasePhotoTicket>>







    //Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(DatabaseProfile: DatabaseProfile)

    @Query("SELECT * FROM profile_table AS pt WHERE pt.profile_user == :user")
    fun getMyProfileInfo(user:String) : LiveData<DatabaseProfile?>





    //friend
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(databaseFriend: DatabaseFriend)

    @Query("DELETE FROM friend_table WHERE friend_code == :friendCode")
    suspend fun deleteFriend(friendCode:String)

    @Query("SELECT * FROM friend_table WHERE friend_my_email == :user ORDER BY friend_nickname DESC")
    fun getAllFriends(user:String) : LiveData<List<DatabaseFriend>>

    @Query("SELECT * FROM friend_table WHERE friend_my_email == :user AND (friend_nickname LIKE '%' || :info || '%' OR friend_code LIKE '%' || :info || '%' ) ")
    fun getSearchingFriends(user:String, info:String) : LiveData<List<DatabaseFriend>>

    //album
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(databaseAlbum: DatabaseAlbum)

    @Query("DELETE FROM album_table WHERE album_id == :id")
    suspend fun deleteAlbum(id:String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(databaseAlbums: List<DatabaseAlbum>)

    @Query("SELECT * FROM album_table WHERE album_user = :user ORDER BY album_name DESC")
    fun getAllAlbum(user:String) : LiveData<List<DatabaseAlbum>>

    @Query("SELECT * FROM album_table WHERE album_id == :id")
    suspend fun getAlbum(id:String) : DatabaseAlbum
}