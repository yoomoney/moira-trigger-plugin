package ru.yandex.money.gradle.plugins.moira.trigger.collect

import java.io.File

/**
 * Files collector that supports fallback values if file not supported by collector.
 * If any exception will be thrown by [collector]'s `collect` method then [fallbackValue] will be returned too.
 */
class SafeFilesCollector<C>(
    private val collector: FilesCollector<C>,
    private val fallbackValue: C
) : FilesCollector<C> {

    override fun isSupported(file: File): Boolean = collector.isSupported(file)

    @Suppress("TooGenericExceptionCaught")
    override fun collect(file: File): C = if (isSupported(file)) {
        try {
            collector.collect(file)
        } catch (e: Exception) {
            fallbackValue
        }
    } else {
        fallbackValue
    }
}
