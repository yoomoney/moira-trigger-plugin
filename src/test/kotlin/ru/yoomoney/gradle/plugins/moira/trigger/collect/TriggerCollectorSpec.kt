package ru.yoomoney.gradle.plugins.moira.trigger.collect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TriggerCollectorSpec {

    @Test
    fun `should return true when file is supported`() {
        // given
        val collector = TriggerCollector(emptyList())

        // when
        val supported = collector.isSupported(File("test.kts"))

        // then
        assertTrue(supported)
    }

    @Test
    fun `should return false when file is not supported`() {
        // given
        val collector = TriggerCollector(emptyList())

        // when
        val supported = collector.isSupported(File("test.json"))

        // then
        assertFalse(supported)
    }

    @Test
    fun `should create single trigger when JSON object is provided`() {
        // given
        val collector = TriggerCollector(emptyList())

        // when
        val triggers = collector.collect(
            File(TriggerCollectorSpec::class.java.getResource("SingleTrigger.kts").path)
        )

        // then
        assertEquals(1, triggers.size)
        assertEquals("test_trigger", triggers[0].id)
    }

    @Test
    fun `should create triggers list when JSON array is provided`() {
        // given
        val collector = TriggerCollector(emptyList())

        // when
        val triggers = collector.collect(
            File(TriggerCollectorSpec::class.java.getResource("TriggersList.kts").path)
        )

        // then
        assertEquals(2, triggers.size)
        assertEquals("test_trigger_1", triggers[0].id)
        assertEquals("test_trigger_2", triggers[1].id)
    }

    @Test(expected = RuntimeException::class)
    fun `should throw an exception when just JSON value is provided`() {
        // given
        val collector = TriggerCollector(emptyList())

        // when
        collector.collect(File(TriggerCollectorSpec::class.java.getResource("InvalidScript.kts").path))
    }

    @Test
    fun `should return empty list when trigger return empty string`() {
        // given
        val collector = TriggerCollector(emptyList())

        // when
        val triggers = collector.collect(File(TriggerCollectorSpec::class.java.getResource("EmptyStringTrigger.kts").path))

        assertEquals(0, triggers.size)
    }
}
