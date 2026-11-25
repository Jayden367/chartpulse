package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TradeSetupEntity::class], version = 2, exportSchema = false)
abstract class ChartPulseDatabase : RoomDatabase() {
    abstract fun tradeSetupDao(): TradeSetupDao

    companion object {
        @Volatile
        private var INSTANCE: ChartPulseDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trade_setups ADD COLUMN profitLossUsd REAL")
            }
        }

        fun getInstance(context: Context): ChartPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChartPulseDatabase::class.java,
                    "chartpulse_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
