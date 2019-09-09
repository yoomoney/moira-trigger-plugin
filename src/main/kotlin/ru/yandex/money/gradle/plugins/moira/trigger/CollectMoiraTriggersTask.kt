package ru.yandex.money.gradle.plugins.moira.trigger

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction
import ru.yandex.money.gradle.plugins.moira.trigger.MoiraTriggerPlugin.Companion.TRIGGERS_FROM_ARTIFACT_DIR
import ru.yandex.money.gradle.plugins.moira.trigger.collect.MoiraTriggersCollector
import java.io.File

import java.nio.file.Paths

/**
 * Task for retrieving triggers from their DSL definitions and printing them to stdout.
 *
 * No attempts to upload triggers to Moira will be made in this task. To upload triggers use [UploadMoiraTriggersTask].
 *
 * This task can be used for debugging your triggers.
 *
 * @author Dmitry Komarov
 * @since 17.06.2019
 */
open class CollectMoiraTriggersTask : DefaultTask() {

    lateinit var dirConfiguration: Configuration
    lateinit var artifactConfiguration: Configuration
    lateinit var extension: MoiraTriggerExtension

    @TaskAction
    fun collectMoiraTriggers() {
        val targetDir = Paths.get(project.projectDir.toString(), extension.dir).toFile()
        targetDir.collectTriggers(dirConfiguration)

        val targetDirArtifact = Paths.get(project.buildDir.toString(), TRIGGERS_FROM_ARTIFACT_DIR).toFile()
        targetDirArtifact.collectTriggers(artifactConfiguration)
    }

    private fun File.collectTriggers(configuration: Configuration) {
        if (!this.exists()) {
            log.lifecycle("Directory ${this.absolutePath} does not exist. Skip.")
            return
        }

        val triggersCollector = MoiraTriggersCollector(configuration.files)
        val collectedTriggers = triggersCollector.collect(this)
        collectedTriggers.forEach { (name, trigger) ->
            log.info("Collected trigger ({}): json={}", name, trigger.toJson().toString(4))
        }
    }

    companion object {

        private val log = Logging.getLogger(CollectMoiraTriggersTask::class.java)
    }
}
