package ru.yoomoney.gradle.plugins.moira.trigger.collect

import java.io.File

/**
 * Collects items from directory using recursive search.
 */
class DirectoryCollector<C>(
    private val collector: FilesCollector<C>
) : FilesCollector<List<C>> {

    override fun isSupported(file: File): Boolean = file.isDirectory

    override fun collect(file: File): List<C> {
        return file.walkTopDown()
            .filter { collector.isSupported(it) && !it.isDirectory }
            .map { collector.collect(it) }
            .toList()
    }
}
