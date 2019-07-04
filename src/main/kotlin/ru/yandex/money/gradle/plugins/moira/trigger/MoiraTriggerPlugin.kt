package ru.yandex.money.gradle.plugins.moira.trigger

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import java.io.File

/**
 * Plugin for compiling and uploading Moira triggers.
 *
 * @author Dmitry Komarov
 * @since 17.06.2019
 */
open class MoiraTriggerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create(EXTENSION_NAME, MoiraTriggerExtension::class.java)
        target.pluginManager.apply(JavaBasePlugin::class.java)
        val configuration = target.configureSourceSets(extension)
        target.afterEvaluate { project ->
            project.createUploadMoiraTriggersTask(configuration, extension)
            project.createCollectMoiraTriggersTask(configuration, extension)
        }
    }

    private fun Project.configureSourceSets(extension: MoiraTriggerExtension): Configuration {
        val sourceSets = convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        val sourceSet = sourceSets.create(SOURCE_SET_NAME)
        sourceSet.java.srcDir(File(extension.dir))

        dependencies.add(
            CONFIGURATION_NAME,
            "org.jetbrains.kotlin:kotlin-stdlib:${KotlinVersion.CURRENT}"
        )
        dependencies.add(
            CONFIGURATION_NAME,
            "org.jetbrains.kotlin:kotlin-reflect:${KotlinVersion.CURRENT}"
        )

        return configurations.getByName(CONFIGURATION_NAME)
    }

    private fun Project.createUploadMoiraTriggersTask(configuration: Configuration, extension: MoiraTriggerExtension) {
        val task = tasks.create(UPLOAD_MOIRA_TRIGGERS_TASK_NAME, UploadMoiraTriggersTask::class.java)
        task.group = "other"
        task.description = "Upload Moira triggers"
        task.configuration = configuration
        task.extension = extension
    }

    private fun Project.createCollectMoiraTriggersTask(configuration: Configuration, extension: MoiraTriggerExtension) {
        val task = tasks.create(COLLECT_MOIRA_TRIGGERS_TASK_NAME, CollectMoiraTriggersTask::class.java)
        task.group = "other"
        task.description = "Collect Moira triggers without uploading"
        task.configuration = configuration
        task.extension = extension
    }

    companion object {

        /**
         * Plugin extension name.
         */
        private const val EXTENSION_NAME = "moira"

        /**
         * Plugin source set name.
         */
        private const val SOURCE_SET_NAME = "moira"

        /**
         * Plugin configuration name.
         */
        private const val CONFIGURATION_NAME = "moiraCompile"

        /**
         * The name of task that uploads triggers to Moira via HTTP calls.
         */
        private const val UPLOAD_MOIRA_TRIGGERS_TASK_NAME = "uploadMoiraTriggers"

        /**
         * The name of task that collects Moira triggers and prints them to stdout.
         */
        private const val COLLECT_MOIRA_TRIGGERS_TASK_NAME = "collectMoiraTriggers"
    }
}
