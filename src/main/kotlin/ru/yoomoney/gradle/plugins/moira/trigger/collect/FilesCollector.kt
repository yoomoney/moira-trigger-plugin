package ru.yoomoney.gradle.plugins.moira.trigger.collect

import java.io.File

/**
 * Interface for collecting files.
 *
 * @author Dmitry Komarov
 * @since 17.06.2019
 */
interface FilesCollector<C> {

    /**
     * Returns `true` if [file] is supported by this collector.
     */
    fun isSupported(file: File): Boolean

    /**
     * Collects some information from given [file].
     */
    fun collect(file: File): C
}
