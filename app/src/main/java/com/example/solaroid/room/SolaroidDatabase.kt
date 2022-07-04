package com.example.solaroid.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.solaroid.data.room.DatabaseFriend
import com.example.solaroid.data.room.DatabasePhotoTicket
import com.example.solaroid.data.room.DatabaseProfile

@Database(entities = [DatabasePhotoTicket::class, DatabaseProfile::class, DatabaseFriend::class], version = 11, exportSchema = false)
abstract class SolaroidDatabase : RoomDatabase() {
    abstract val photoTicketDao : DatabasePhotoTicketDao

    companion object {
        @Volatile
        private var INSTANCE : SolaroidDatabase? = null

        fun getInstance(context: Context) : SolaroidDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if(instance==null) {
                    instance = Room.databaseBuilder(context.applicationContext, SolaroidDatabase::class.java, "solaroid_history_database")
                        .fallbackToDestructiveMigration()
                        .build()
                   INSTANCE = instance
                }
                return instance
            }
        }
    }
}


