package com.eewill.discgolftraining.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `approach_rounds` (
                `id` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `targetDistanceFeet` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `approach_round_discs` (
                `roundId` TEXT NOT NULL,
                `discId` TEXT NOT NULL,
                `sortIndex` INTEGER NOT NULL,
                PRIMARY KEY(`roundId`, `discId`),
                FOREIGN KEY(`roundId`) REFERENCES `approach_rounds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`discId`) REFERENCES `discs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_approach_round_discs_roundId` ON `approach_round_discs`(`roundId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_approach_round_discs_discId` ON `approach_round_discs`(`discId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `approach_throws` (
                `id` TEXT NOT NULL,
                `roundId` TEXT NOT NULL,
                `index` INTEGER NOT NULL,
                `discId` TEXT,
                `landingDistanceFeet` REAL NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`roundId`) REFERENCES `approach_rounds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`discId`) REFERENCES `discs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_approach_throws_roundId` ON `approach_throws`(`roundId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_approach_throws_discId` ON `approach_throws`(`discId`)")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `discs` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `discs` SET `sortOrder` = `createdAt`")
    }
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new discs table.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `discs` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // Additive column on rounds.
        db.execSQL("ALTER TABLE `rounds` ADD COLUMN `discDataMode` TEXT NOT NULL DEFAULT 'NONE'")

        // SQLite can't ALTER TABLE to add a new foreign key, so recreate `throws`
        // with the full target schema and copy the data over.
        db.execSQL(
            """
            CREATE TABLE `throws_new` (
                `id` TEXT NOT NULL,
                `roundId` TEXT NOT NULL,
                `index` INTEGER NOT NULL,
                `x` REAL NOT NULL,
                `y` REAL NOT NULL,
                `isHit` INTEGER NOT NULL,
                `discType` TEXT,
                `discId` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`roundId`) REFERENCES `rounds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`discId`) REFERENCES `discs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `throws_new` (`id`, `roundId`, `index`, `x`, `y`, `isHit`)
            SELECT `id`, `roundId`, `index`, `x`, `y`, `isHit` FROM `throws`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `throws`")
        db.execSQL("ALTER TABLE `throws_new` RENAME TO `throws`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_throws_roundId` ON `throws`(`roundId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_throws_discId` ON `throws`(`discId`)")
    }
}
