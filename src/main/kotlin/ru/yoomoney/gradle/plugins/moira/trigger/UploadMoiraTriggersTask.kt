package ru.yoomoney.gradle.plugins.moira.trigger

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import ru.yoomoney.gradle.plugins.moira.trigger.MoiraTriggerPlugin.Companion.TRIGGERS_FROM_ARTIFACT_DIR
import ru.yoomoney.gradle.plugins.moira.trigger.collect.MoiraTriggersCollector
import ru.yoomoney.gradle.plugins.moira.trigger.upload.MoiraUploader
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
    @Input
    lateinit var extension: MoiraTriggerExtension

    @TaskAction
    internal fun uploadMoiraTriggers() {
        val artifactSourceSet = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
            .getByName("moiraFromArtifact")
        val dirSourceSet = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
            .getByName("moiraFromDir")

        val uploader = MoiraUploader(url = extension.url!!, login = extension.login, password = extension.password)

        val targetDir = Paths.get(project.projectDir.toString(), extension.dir).toFile()
        targetDir.uploadTriggers(dirSourceSet, uploader)

        val targetDirArtifact = Paths.get(project.buildDir.toString(), TRIGGERS_FROM_ARTIFACT_DIR).toFile()
        targetDirArtifact.uploadTriggers(artifactSourceSet, uploader)
    }

    private fun File.uploadTriggers(sourceSet: SourceSet, uploader: MoiraUploader) {
        if (!this.exists()) {
            log.lifecycle("Directory ${this.absolutePath} does not exist. Skip.")
            return
        }

        val triggersCollector = MoiraTriggersCollector(classPathFiles = sourceSet.compileClasspath.files)
        val triggersToUpload = triggersCollector
                .collect(this)
                .map { it.second }
        uploader.upload(triggersToUpload)
    }

    companion object {
        private val log = Logging.getLogger(UploadMoiraTriggersTask::class.java)
    }
}
