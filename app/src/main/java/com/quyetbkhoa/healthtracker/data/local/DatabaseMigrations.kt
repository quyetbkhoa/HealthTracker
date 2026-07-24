package com.quyetbkhoa.healthtracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE foods ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_3_4
)
