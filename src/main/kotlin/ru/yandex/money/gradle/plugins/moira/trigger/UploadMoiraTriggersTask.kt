package ru.yandex.money.gradle.plugins.moira.trigger

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction
import ru.yandex.money.gradle.plugins.moira.trigger.MoiraTriggerPlugin.Companion.TRIGGERS_FROM_ARTIFACT_DIR
import ru.yandex.money.gradle.plugins.moira.trigger.collect.MoiraTriggersCollector
import ru.yandex.money.gradle.plugins.moira.trigger.upload.MoiraUploader
import java.io.File

import java.nio.file.Paths

/**
 * Task for uploading triggers to Moira.
 *
 * To debug your triggers use [CollectMoiraTriggersTask].
 *
 * @author Dmitry Komarov
 * @since 17.06.2019
 */
open class UploadMoiraTriggersTask : DefaultTask() {

    lateinit var dirConfiguration: Configuration
    lateinit var artifactConfiguration: Configuration
    lateinit var extension: MoiraTriggerExtension

    private lateinit var uploader: MoiraUploader

    @TaskAction
    internal fun uploadMoiraTriggers() {
        uploader = MoiraUploader(url = extension.url!!, login = extension.login, password = extension.password)

        val targetDir = Paths.get(project.projectDir.toString(), extension.dir).toFile()
        targetDir.uploadTriggers(dirConfiguration)

        val targetDirArtifact = Paths.get(project.buildDir.toString(), TRIGGERS_FROM_ARTIFACT_DIR).toFile()
        targetDirArtifact.uploadTriggers(artifactConfiguration)
    }

    private fun File.uploadTriggers(configuration: Configuration) {
        if (!this.exists()) {
            log.lifecycle("Directory ${this.absolutePath} does not exist. Skip.")
            return
        }

        val triggersCollector = MoiraTriggersCollector(configuration.files)
        val triggersToUpload = triggersCollector
                .collect(this)
                .map { it.second }
        uploader.upload(triggersToUpload)
    }

    companion object {
        private val log = Logging.getLogger(UploadMoiraTriggersTask::class.java)
    }
}
