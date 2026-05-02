package com.eewill.discgolftraining.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14: Migration = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `round_discs` (
                `roundId` TEXT NOT NULL,
                `discId` TEXT NOT NULL,
                `sortIndex` INTEGER NOT NULL,
                PRIMARY KEY(`roundId`, `discId`),
                FOREIGN KEY(`roundId`) REFERENCES `rounds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`discId`) REFERENCES `discs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_round_discs_roundId` ON `round_discs`(`roundId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_round_discs_discId` ON `round_discs`(`discId`)")
    }
}

val MIGRATION_12_13: Migration = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `discs` ADD COLUMN `isActive` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `discs` ADD COLUMN `includeInStats` INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_11_12: Migration = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `throws` ADD COLUMN `flightModifier` TEXT")
    }
}

val MIGRATION_10_11: Migration = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `rounds` ADD COLUMN `minDistanceFeet` REAL")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `round_short_discs` (
                `roundId` TEXT NOT NULL,
                `discId` TEXT NOT NULL,
                PRIMARY KEY(`roundId`, `discId`),
                FOREIGN KEY(`roundId`) REFERENCES `rounds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`discId`) REFERENCES `discs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_round_short_discs_roundId` ON `round_short_discs`(`roundId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_round_short_discs_discId` ON `round_short_discs`(`discId`)")
    }
}

val MIGRATION_9_10: Migration = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `discs` ADD COLUMN `notes` TEXT")
    }
}

val MIGRATION_8_9: Migration = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `putting_rounds` (
                `id` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `minDistanceFeet` REAL NOT NULL,
                `maxDistanceFeet` REAL NOT NULL,
                `intervalFeet` REAL NOT NULL,
                `throwsPerPosition` INTEGER NOT NULL,
                `notes` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `putting_throws` (
                `id` TEXT NOT NULL,
                `roundId` TEXT NOT NULL,
                `distanceFeet` REAL NOT NULL,
                `positionIndex` INTEGER NOT NULL,
                `throwIndex` INTEGER NOT NULL,
                `result` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`roundId`) REFERENCES `putting_rounds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_putting_throws_roundId` ON `putting_throws`(`roundId`)")
    }
}

val MIGRATION_7_8: Migration = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `rounds` ADD COLUMN `notes` TEXT")
        db.execSQL("ALTER TABLE `approach_rounds` ADD COLUMN `notes` TEXT")
    }
}

val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `approach_rounds` ADD COLUMN `startLat` REAL")
        db.execSQL("ALTER TABLE `approach_rounds` ADD COLUMN `startLng` REAL")
    }
}

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `approach_rounds` ADD COLUMN `targetSizeFeet` REAL")
    }
}

val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `approach_rounds` ADD COLUMN `targetLat` REAL")
        db.execSQL("ALTER TABLE `approach_rounds` ADD COLUMN `targetLng` REAL")
        db.execSQL("ALTER TABLE `approach_throws` ADD COLUMN `landingLat` REAL")
        db.execSQL("ALTER TABLE `approach_throws` ADD COLUMN `landingLng` REAL")
    }
}

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
