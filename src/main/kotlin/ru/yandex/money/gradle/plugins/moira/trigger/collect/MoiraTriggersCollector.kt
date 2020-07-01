package ru.yandex.money.gradle.plugins.moira.trigger.collect

import ru.yandex.money.moira.client.triggers.Trigger
import java.io.File

/**
 * Collector for Moira triggers DSL definitions. This collector is used in Gradle tasks.
 *
 * This collector uses recursive search due to [DirectoryCollector].
 */
class MoiraTriggersCollector(classPathFiles: Collection<File>) : FilesCollector<List<Pair<String, Trigger>>> {

    private val collector = DirectoryCollector(
        CollectorWithFileName(TriggerCollector(classPathFiles))
    )

    override fun isSupported(file: File): Boolean = collector.isSupported(file)

    override fun collect(file: File): List<Pair<String, Trigger>> = collector.collect(file)
        .filter { (_, triggers) -> triggers.isNotEmpty() }
        .flatMap { (name, triggers) ->
            triggers.map { "File: [${File(name).toRelativeString(file)}] Trigger: [${it.name}]" to it }
        }
        .distinctBy { (_, trigger) -> trigger.name }
}
