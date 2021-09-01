package ru.yoomoney.gradle.plugins.moira.trigger.collect

import java.io.File
import java.io.FileFilter

/**
 * Collects items from directory using recursive search.
 *
 * Note that [maxDepth] = 1 means that only root will be scanned. Depth is unlimited by default.
 */
class DirectoryCollector<C>(
    private val collector: FilesCollector<C>,
    private val recursive: Boolean = true,
    private val maxDepth: Int = Integer.MAX_VALUE
) : FilesCollector<List<C>> {

    override fun isSupported(file: File): Boolean = file.isDirectory

    override fun collect(file: File): List<C> = collectRecursive(file, 0)

    private fun collectRecursive(file: File, depth: Int): List<C> {
        if (depth >= maxDepth) {
            return emptyList()
        }
        val files = file.listFiles(FileFilter { collector.isSupported(it) })

        var collectedItems = files.map { collector.collect(it) }
        if (recursive) {
            val directories = file.listFiles(FileFilter { it.isDirectory })
            collectedItems += directories.flatMap { collectRecursive(it, depth + 1) }
        }
        return collectedItems
    }
}
