package com.example

import com.example.data.local.Converters
import com.example.data.model.Subtask
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testSubtasksJsonSerialization() {
        val original = listOf(
            Subtask("1", "Read Chapter 3", false),
            Subtask("2", "Solve Past Paper 2023", true)
        )
        val json = Converters.subtasksToJson(original)
        val deserialized = Converters.jsonToSubtasks(json)

        assertEquals(2, deserialized.size)
        assertEquals("Read Chapter 3", deserialized[0].title)
        assertEquals(false, deserialized[0].isCompleted)
        assertEquals("Solve Past Paper 2023", deserialized[1].title)
        assertEquals(true, deserialized[1].isCompleted)
    }

    @Test
    fun testListToStringConversion() {
        val tags = listOf("Urgent", "Study", "Math")
        val serialized = Converters.listToString(tags)
        val restored = Converters.stringToList(serialized)
        assertEquals(tags, restored)
    }
}
