package ru.yoomoney.gradle.plugins.moira.trigger

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import ru.yoomoney.gradle.plugins.moira.trigger.MoiraTriggerPlugin.Companion.TRIGGERS_FROM_ARTIFACT_DIR
import ru.yoomoney.gradle.plugins.moira.trigger.collect.MoiraTriggersCollector
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
    @Input
    lateinit var extension: MoiraTriggerExtension

    @TaskAction
    fun collectMoiraTriggers() {
        val artifactSourceSet = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
            .getByName("moiraFromArtifact")
        val dirSourceSet = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
            .getByName("moiraFromDir")

        val targetDir = Paths.get(project.projectDir.toString(), extension.dir).toFile()
        targetDir.collectTriggers(dirSourceSet)

        val targetDirArtifact = Paths.get(project.buildDir.toString(), TRIGGERS_FROM_ARTIFACT_DIR).toFile()
        targetDirArtifact.collectTriggers(artifactSourceSet)
    }

    private fun File.collectTriggers(sourceSet: SourceSet) {
        if (!this.exists()) {
            log.lifecycle("Directory ${this.absolutePath} does not exist. Skip.")
            return
        }

        val triggersCollector = MoiraTriggersCollector(classPathFiles = sourceSet.compileClasspath.files)
        val collectedTriggers = triggersCollector.collect(this)
        collectedTriggers.forEach { (name, trigger) ->
            log.info("Collected trigger ({}): json={}", name, trigger.toJson().toString(4))
        }
    }

    companion object {
        private val log = Logging.getLogger(CollectMoiraTriggersTask::class.java)
    }
}
