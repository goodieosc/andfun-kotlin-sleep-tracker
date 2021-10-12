package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.security.AccessControlContext


@Database(entities = [SleepNight::class], version = 1, exportSchema = false) //Can add multiple entities.
abstract class SleepDatabase : RoomDatabase(){  //Class is abstract extending as its not access directly, its accessed from Room

    //Define that DAO is used, can add multiple DAO's
    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: SleepDatabase? = null  //INSTANCE keeps a reference to the database to connections dont need to be opened repeatedly.

        fun getInstance(context: Context) : SleepDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {   //Check if there is already a database
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SleepDatabase::class.java,
                        "sleep_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance

            }
                return instance
            }



        }

    }




}
