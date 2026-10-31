package com.ascdev.checkmo.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CheckmoDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsEntriesAndLeavesLocationEmpty() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO time_entries (id, user_id, company_id, type, timestamp, sync_status)
                VALUES ('e1', 'uid', 'c1', 'CLOCK_IN', 1000, 'PENDING')
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        db.query("SELECT id, latitude, longitude, accuracy_meters FROM time_entries").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("e1", cursor.getString(0))
            assertTrue(cursor.isNull(1))
            assertTrue(cursor.isNull(2))
            assertTrue(cursor.isNull(3))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
