package ru.yoomoney.gradle.plugins.moira.trigger.collect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.script.ScriptException

class MoiraTriggersCollectorSpec {

    @Test
    fun `should return true when folder is provided`() {
        // given
        val folder = File(MoiraTriggersCollectorSpec::class.java.getResource("test").file)
        val collector = MoiraTriggersCollector(emptyList())

        // when
        val supported = collector.isSupported(folder)

        // then
        assertTrue(supported)
    }

    @Test
    fun `should return false when file is provided`() {
        // given
        val file = File("test.txt")
        val collector = MoiraTriggersCollector(emptyList())

        // when
        val supported = collector.isSupported(file)

        // then
        assertFalse(supported)
    }

    @Test
    fun `should return triggers list when folder is provided`() {
        // given
        val folder = File(MoiraTriggersCollectorSpec::class.java.getResource("test/simple").file)
        val collector = MoiraTriggersCollector(emptyList())

        // when
        val triggers = collector.collect(folder)

        // then
        assertEquals(3, triggers.size)
        assertEquals(
            setOf(
                "File: [SingleTrigger.kts] Trigger: [Test trigger]",
                "File: [recursive${File.separator}TriggersList.kts] Trigger: [Test trigger 1 (succeeded)]",
                "File: [recursive${File.separator}TriggersList.kts] Trigger: [Test trigger 2 (failed)]"
            ),
            triggers.map { (name, _) -> name }.toSet()
        )
        assertEquals(
            setOf("Test trigger", "Test trigger 1 (succeeded)", "Test trigger 2 (failed)"),
            triggers.map { (_, trigger) -> trigger.name }.toSet()
        )
    }

    @Test(expected = ScriptException::class)
    fun `should throw script exception when trigger throw any exception`() {
        // given
        val folder = File(MoiraTriggersCollectorSpec::class.java.getResource("test/throw").file)
        val collector = MoiraTriggersCollector(emptyList())

        // when
        collector.collect(folder)
    }
}
