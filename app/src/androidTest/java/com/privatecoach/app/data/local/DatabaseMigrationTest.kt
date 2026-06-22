package com.privatecoach.app.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "migration-test.db"

    @After fun cleanup() = context.deleteDatabase(name)

    @Test fun migrationChainPreservesFeelingAndMergesSameDayRows() {
        open(1, object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sync_id TEXT, date TEXT NOT NULL, type TEXT NOT NULL, body_part TEXT, feeling TEXT, ai_summary TEXT, raw_transcript TEXT, audio_file_path TEXT, input_mode TEXT NOT NULL, template_id INTEGER, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)""")
                db.execSQL("""CREATE TABLE exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, workout_id INTEGER NOT NULL, name TEXT NOT NULL, weight REAL, weight_unit TEXT NOT NULL, sets INTEGER, reps INTEGER, duration INTEGER, distance REAL, sort_order INTEGER NOT NULL, notes TEXT, FOREIGN KEY(workout_id) REFERENCES workouts(id) ON DELETE CASCADE)""")
                db.execSQL("INSERT INTO workouts(id,date,type,feeling,input_mode,created_at,updated_at) VALUES(1,'2026-06-22','STRENGTH','GOOD','MANUAL',1,1)")
                db.execSQL("INSERT INTO workouts(id,date,type,feeling,input_mode,created_at,updated_at) VALUES(2,'2026-06-22','STRENGTH','TIRED','MANUAL',2,2)")
                db.execSQL("INSERT INTO exercises(id,workout_id,name,weight_unit,sort_order) VALUES(1,1,'卧推','kg',0)")
                db.execSQL("INSERT INTO exercises(id,workout_id,name,weight_unit,sort_order) VALUES(2,2,'深蹲','kg',0)")
            }
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }).close()

        val migrated = open(3, object : SupportSQLiteOpenHelper.Callback(3) {
            override fun onCreate(db: SupportSQLiteDatabase) = Unit
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
            }
        })

        migrated.readableDatabase.query("SELECT COUNT(*) FROM workouts").use { it.moveToFirst(); assertEquals(1, it.getInt(0)) }
        migrated.readableDatabase.query("SELECT feeling FROM exercises WHERE name='卧推'").use { it.moveToFirst(); assertEquals("GOOD", it.getString(0)) }
        migrated.readableDatabase.query("SELECT COUNT(DISTINCT workout_id) FROM exercises").use { it.moveToFirst(); assertEquals(1, it.getInt(0)) }
        migrated.close()
    }

    private fun open(version: Int, callback: SupportSQLiteOpenHelper.Callback): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(callback).build()
        ).also { it.writableDatabase }
}
