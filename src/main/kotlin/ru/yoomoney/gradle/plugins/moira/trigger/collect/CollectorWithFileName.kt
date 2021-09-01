package ru.yoomoney.gradle.plugins.moira.trigger.collect

import java.io.File

/**
 * Collects item from file and provides the absolute name of this file.
 */
class CollectorWithFileName<C>(private val collector: FilesCollector<C>) : FilesCollector<ItemWithFileName<C>> {

    override fun isSupported(file: File): Boolean = collector.isSupported(file)

    override fun collect(file: File): ItemWithFileName<C> = ItemWithFileName(file.absolutePath, collector.collect(file))
}
