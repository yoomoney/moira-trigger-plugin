package ru.yandex.money.gradle.plugins.moira.trigger.collect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CollectorWithFileNameSpec {

    @Test
    fun `should return true when delegated collector supports file`() {
        // given
        val delegate = TestCollector()
        val collector = CollectorWithFileName(delegate)

        val file = File("test.txt")

        // when
        val supported = collector.isSupported(file)

        // then
        assertTrue(supported)
    }

    @Test
    fun `should return false when delegate collector does not support file`() {
        // given
        val delegate = TestCollector(supported = false)
        val collector = CollectorWithFileName(delegate)

        val file = File("test.txt")

        // when
        val supported = collector.isSupported(file)

        // then
        assertFalse(supported)
    }

    @Test
    fun `should return collected item with name when collecting file`() {
        // given
        val delegate = TestCollector()
        val collector = CollectorWithFileName(delegate)

        val file = File("test.txt")

        // when
        val itemWithFileName = collector.collect(file)

        // then
        assertEquals(file.absolutePath, itemWithFileName.fileName)
        assertEquals("item", itemWithFileName.item)
    }

    class TestCollector(private val supported: Boolean = true) : FilesCollector<String> {

        override fun isSupported(file: File): Boolean = supported

        override fun collect(file: File): String = "item"
    }
}
