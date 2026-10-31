package com.ascdev.checkmo.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ascdev.checkmo.domain.model.SyncStatus
import com.ascdev.checkmo.domain.model.TimeEntryType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimeEntryDaoTest {

    private lateinit var database: CheckmoDatabase
    private lateinit var dao: TimeEntryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CheckmoDatabase::class.java,
        ).build()
        dao = database.timeEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(id: String, timestamp: Long, userId: String = "uid", type: TimeEntryType = TimeEntryType.CLOCK_IN) =
        TimeEntryEntity(id, userId, "c1", type, timestamp, SyncStatus.PENDING)

    @Test
    fun observeBetween_returnsOnlyUserEntriesInRangeOrderedByTime() = runTest {
        dao.insert(entity("late", timestamp = 300))
        dao.insert(entity("early", timestamp = 100))
        dao.insert(entity("outside", timestamp = 500))
        dao.insert(entity("other-user", timestamp = 200, userId = "other"))

        val result = dao.observeBetween("uid", fromMillis = 100, toMillis = 500).first()

        assertEquals(listOf("early", "late"), result.map { it.id })
    }

    @Test
    fun observeLatestBefore_returnsMostRecentEntryStrictlyBefore() = runTest {
        dao.insert(entity("a", timestamp = 100))
        dao.insert(entity("b", timestamp = 200))
        dao.insert(entity("c", timestamp = 300))

        assertEquals("b", dao.observeLatestBefore("uid", beforeMillis = 300).first()?.id)
        assertNull(dao.observeLatestBefore("uid", beforeMillis = 100).first())
    }

    @Test
    fun getLatest_storesEnumsAndReturnsNewestEntry() = runTest {
        dao.insert(entity("in", timestamp = 100))
        dao.insert(entity("break", timestamp = 200, type = TimeEntryType.BREAK_START))

        val latest = dao.getLatest("uid")

        assertEquals(TimeEntryType.BREAK_START, latest?.type)
        assertEquals(SyncStatus.PENDING, latest?.syncStatus)
    }
}
