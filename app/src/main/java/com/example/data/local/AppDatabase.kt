package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        JobEntity::class,
        SettingsEntity::class,
        TransactionEntity::class,
        CommunityPostEntity::class,
        CommunityCommentEntity::class,
        DirectMessageEntity::class,
        ModerationReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun settingsDao(): SettingsDao
    abstract fun transactionDao(): TransactionDao
    abstract fun communityDao(): CommunityDao
    abstract fun moderationDao(): ModerationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "syhakal_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
