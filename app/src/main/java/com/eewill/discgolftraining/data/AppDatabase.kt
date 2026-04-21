package com.eewill.discgolftraining.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RoundEntity::class,
        ThrowEntity::class,
        DiscEntity::class,
        RoundShortDiscEntity::class,
        ApproachRoundEntity::class,
        ApproachRoundDiscEntity::class,
        ApproachThrowEntity::class,
        PuttingRoundEntity::class,
        PuttingThrowEntity::class,
    ],
    version = 12,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roundDao(): RoundDao
    abstract fun discDao(): DiscDao
    abstract fun approachRoundDao(): ApproachRoundDao
    abstract fun puttingRoundDao(): PuttingRoundDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "discgolf.db",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .build()
    }
}
