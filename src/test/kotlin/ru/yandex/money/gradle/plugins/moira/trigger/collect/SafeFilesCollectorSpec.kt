package ru.yandex.money.gradle.plugins.moira.trigger.collect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SafeFilesCollectorSpec {

    @Test
    fun `should return true when delegated collector supports file`() {
        // given
        val delegate = TestCollector()
        val collector = SafeFilesCollector(delegate, "test")

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
        val collector = SafeFilesCollector(delegate, "test")

        val file = File("test.txt")

        // when
        val supported = collector.isSupported(file)

        // then
        assertFalse(supported)
    }

    @Test
    fun `should return fallback value when file not supported by delegate`() {
        // given
        val delegate = TestCollector(supported = false)
        val collector = SafeFilesCollector(delegate, "test")

        val file = File("test.txt")

        // when
        val item = collector.collect(file)

        // then
        assertEquals("test", item)
    }

    @Test
    fun `should return fallback value when delegate throws exception while collecting items`() {
        val delegate = TestCollector(shouldThrowWhenCollect = true)
        val collector = SafeFilesCollector(delegate, "test")

        val file = File("test.txt")

        // when
        val item = collector.collect(file)

        // then
        assertEquals("test", item)
    }

    @Test
    fun `should return collected item by delegate when no exception has been thrown`() {
        val delegate = TestCollector()
        val collector = SafeFilesCollector(delegate, "test")

        val file = File("test.txt")

        // when
        val item = collector.collect(file)

        // then
        assertEquals("test.txt", item)
    }

    class TestCollector(
        private val supported: Boolean = true,
        private val shouldThrowWhenCollect: Boolean = false
    ) : FilesCollector<String> {

        override fun isSupported(file: File): Boolean = supported

        override fun collect(file: File): String {
            if (shouldThrowWhenCollect) {
                throw IllegalArgumentException()
            }
            return file.name
        }
    }
}
