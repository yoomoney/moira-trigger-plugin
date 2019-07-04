package ru.yandex.money.gradle.plugins.moira.trigger.collect

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DirectoryCollectorSpec {

    @get:Rule
    val directory = TemporaryFolder()

    /**
     * After calling this method temp fold should have given structure:
     * ```
     * +--tempFolder/
     * |  +--dir2/
     * |  |  +--lvl1/
     * |  |  |  +--file1.supported
     * |  |  |  +--file2.supported
     * |  |  |  +--file3.unsupported
     * |  |  +--lvl2/
     * |  |  |  +--file1.supported
     * |  |  |  +--file2.supported
     * |  |  |  +--file3.unsupported
     * |  |  +--file1.supported
     * |  |  +--file2.supported
     * |  |  +--file3.unsupported
     * |  +--dir1/
     * |  |  +--lvl1/
     * |  |  |  +--file1.supported
     * |  |  |  +--file2.supported
     * |  |  |  +--file3.unsupported
     * |  |  +--lvl2/
     * |  |  |  +--file1.supported
     * |  |  |  +--file2.supported
     * |  |  |  +--file3.unsupported
     * |  |  +--file1.supported
     * |  |  +--file2.supported
     * |  |  +--file3.unsupported
     * |  +--file1.supported
     * |  +--file2.supported
     * |  +--file3.unsupported
     * ```
     */
    @Before
    fun before() {
        // fill in root
        fillInDirectory()

        // fill in 2 directories under root
        fillInDirectory(name = "dir1")
        fillInDirectory(name = "dir2")

        // fill in 2 directories under dir1
        fillInDirectory(name = "dir1/lvl1")
        fillInDirectory(name = "dir1/lvl2")

        // fill in 2 directories under dir2
        fillInDirectory(name = "dir2/lvl1")
        fillInDirectory(name = "dir2/lvl2")
    }

    private fun fillInDirectory(name: String? = null) {
        // create directory
        if (name != null) {
            directory.newFolder(*name.split("/").toTypedArray())
        }

        val prefix = if (name == null) "" else "$name/"

        // create 2 supported files
        directory.newFile("${prefix}file1.supported")
        directory.newFile("${prefix}file2.supported")

        // create 1 unsupported file
        directory.newFile("${prefix}file3.unsupported")
    }

    @Test
    fun `should return only supported files in root when recursive option is turned off`() {
        // given
        val delegate = TestCollector(directory.root)
        val collector = DirectoryCollector(collector = delegate, recursive = false)

        // when
        val collected = collector.collect(directory.root)

        // then
        assertEquals(setOf("file1.supported", "file2.supported"), collected.toSet())
    }

    @Test
    fun `should return supported files from root and dir1 and dir2 when recursive option is turned on and depth is 2`() {
        // given
        val delegate = TestCollector(directory.root)
        val collector = DirectoryCollector(collector = delegate, maxDepth = 2)

        // when
        val collected = collector.collect(directory.root)

        // then
        assertEquals(
            setOf(
                "file1.supported",
                "file2.supported",
                "dir1/file1.supported",
                "dir1/file2.supported",
                "dir2/file1.supported",
                "dir2/file2.supported"
            ),
            collected.toSet()
        )
    }

    @Test
    fun `should return all supported files from root recursevely when recursive option is turned on and depthe is 3`() {
        // given
        val delegate = TestCollector(directory.root)
        val collector = DirectoryCollector(collector = delegate, maxDepth = 3)

        // when
        val collected = collector.collect(directory.root)

        // then
        assertEquals(
            setOf(
                "file1.supported",
                "file2.supported",

                "dir1/file1.supported",
                "dir1/file2.supported",
                "dir1/lvl1/file1.supported",
                "dir1/lvl1/file2.supported",
                "dir1/lvl2/file1.supported",
                "dir1/lvl2/file2.supported",

                "dir2/file1.supported",
                "dir2/file2.supported",
                "dir2/lvl1/file1.supported",
                "dir2/lvl1/file2.supported",
                "dir2/lvl2/file1.supported",
                "dir2/lvl2/file2.supported"
            ),
            collected.toSet()
        )
    }

    class TestCollector(private val root: File) : FilesCollector<String> {

        override fun isSupported(file: File): Boolean = file.extension == "supported"

        override fun collect(file: File): String = file.toRelativeString(root)
    }
}
