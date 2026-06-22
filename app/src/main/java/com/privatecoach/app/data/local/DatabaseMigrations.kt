package com.privatecoach.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercises ADD COLUMN feeling TEXT")
        db.execSQL(
            """
            UPDATE exercises
            SET feeling = (
                SELECT workouts.feeling FROM workouts WHERE workouts.id = exercises.workout_id
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val hasLegacyFeeling = db.query("PRAGMA table_info(workouts)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == "feeling") found = true
            }
            found
        }
        if (!hasLegacyFeeling) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN feeling TEXT")
        }

        db.execSQL(
            """
            UPDATE workouts
            SET ai_summary = (
                    SELECT GROUP_CONCAT(ai_summary, char(10) || '---' || char(10))
                    FROM workouts source
                    WHERE source.date = workouts.date AND source.ai_summary IS NOT NULL AND source.ai_summary != ''
                ),
                raw_transcript = (
                    SELECT GROUP_CONCAT(raw_transcript, char(10))
                    FROM workouts source
                    WHERE source.date = workouts.date AND source.raw_transcript IS NOT NULL AND source.raw_transcript != ''
                ),
                updated_at = (SELECT MAX(updated_at) FROM workouts source WHERE source.date = workouts.date)
            WHERE id IN (SELECT MIN(id) FROM workouts GROUP BY date)
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE exercises
            SET workout_id = (
                SELECT MIN(target.id)
                FROM workouts target
                WHERE target.date = (SELECT source.date FROM workouts source WHERE source.id = exercises.workout_id)
            )
            """.trimIndent()
        )
        db.execSQL("DELETE FROM workouts WHERE id NOT IN (SELECT MIN(id) FROM workouts GROUP BY date)")
        db.execSQL(
            """
            UPDATE exercises
            SET sort_order = (
                SELECT COUNT(*) - 1 FROM exercises ordered
                WHERE ordered.workout_id = exercises.workout_id AND ordered.id <= exercises.id
            )
            """.trimIndent()
        )
        db.execSQL("DROP INDEX IF EXISTS index_workouts_date")
        db.execSQL("CREATE UNIQUE INDEX index_workouts_date ON workouts(date)")
    }
}
