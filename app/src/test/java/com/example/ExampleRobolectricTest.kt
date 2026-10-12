package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CalendarEvent
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.repository.CalendarRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TIMEFLOW", appName)
    }

    @Test
    fun `test schedule conflict detection`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = CalendarRepository(context)

        val existing = listOf(
            CalendarEvent(
                id = "1",
                userId = "u1",
                title = "Math Revision",
                startTimeMillis = 1000L,
                endTimeMillis = 2000L
            )
        )

        // Conflict overlapping (1500 to 2500 overlaps with 1000 to 2000)
        val conflicts = repo.checkConflicts(existing, 1500L, 2500L)
        assertEquals(1, conflicts.size)

        // No conflict (3000 to 4000)
        val noConflicts = repo.checkConflicts(existing, 3000L, 4000L)
        assertTrue(noConflicts.isEmpty())
    }

    @Test
    fun `test task creation and default completion status`() {
        val task = TaskItem(
            id = "t1",
            title = "Biology Study Plan",
            priority = Priority.HIGH
        )
        assertFalse(task.isCompleted)
        assertEquals(Priority.HIGH, task.priority)
    }
}
