package ru.yandex.money.gradle.plugins.moira.trigger

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction
import ru.yandex.money.gradle.plugins.moira.trigger.collect.MoiraTriggersCollector
import ru.yandex.money.gradle.plugins.moira.trigger.upload.MoiraUploader

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

    lateinit var configuration: Configuration
    lateinit var extension: MoiraTriggerExtension

    @TaskAction
    internal fun uploadMoiraTriggers() {
        val targetDir = Paths.get(project.projectDir.toString(), extension.dir).toFile()

        val triggersCollector = MoiraTriggersCollector(configuration.files)
        val triggersToUpload = triggersCollector.collect(targetDir).map { it.second }

        val uploader = MoiraUploader(url = extension.url!!, login = extension.login, password = extension.password)
        uploader.upload(triggersToUpload)
    }
}
