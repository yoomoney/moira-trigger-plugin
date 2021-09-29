package ru.yoomoney.gradle.plugins.moira.trigger

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import java.io.File
import java.nio.file.Paths
import kotlin.streams.toList

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
        val dirConfiguration = target.configureDirSourceSets(extension)
        val artifactConfiguration = target.configureArtifactSourceSets()

        val triggersConfiguration = target.configurations
                .maybeCreate("${ARTIFACT_TRIGGERS_CONFIGURATION_NAME}Compile")

        target.afterEvaluate { project ->
            artifactConfiguration.extendsFrom(triggersConfiguration)
            project.createUploadMoiraTriggersTask(dirConfiguration, artifactConfiguration, extension)
            project.createCollectMoiraTriggersTask(dirConfiguration, artifactConfiguration, extension)
            project.createExtractTriggersTask(triggersConfiguration)
        }
    }

    private fun Project.configureDirSourceSets(extension: MoiraTriggerExtension): Configuration {
        val sourceSets = convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        val sourceSet = sourceSets.create(DIR_SOURCE_SET_NAME)
        sourceSet.java.srcDir(File(extension.dir))

        return addKotlinDependencies("${DIR_SOURCE_SET_NAME}Compile")
    }

    private fun Project.configureArtifactSourceSets(): Configuration {
        val sourceSets = convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        val sourceSet = sourceSets.create(ARTIFACT_SOURCE_SET_NAME)
        sourceSet.java.srcDir(Paths.get(buildDir.toString(), TRIGGERS_FROM_ARTIFACT_DIR).toString())

        return addKotlinDependencies("${ARTIFACT_SOURCE_SET_NAME}Compile")
    }

    private fun Project.addKotlinDependencies(configurationName: String): Configuration {
        dependencies.add(
                configurationName,
                "org.jetbrains.kotlin:kotlin-stdlib:${KotlinVersion.CURRENT}"
        )
        dependencies.add(
                configurationName,
                "org.jetbrains.kotlin:kotlin-reflect:${KotlinVersion.CURRENT}"
        )
        return configurations.getByName(configurationName)
    }

    private fun Project.createUploadMoiraTriggersTask(
        dirConfiguration: Configuration,
        artifactConfiguration: Configuration,
        extension: MoiraTriggerExtension
    ) {
        val task = tasks.create(UPLOAD_MOIRA_TRIGGERS_TASK_NAME, UploadMoiraTriggersTask::class.java)
        task.group = "other"
        task.description = "Upload Moira triggers"
        task.extension = extension
    }

    private fun Project.createCollectMoiraTriggersTask(
        dirConfiguration: Configuration,
        artifactConfiguration: Configuration,
        extension: MoiraTriggerExtension
    ) {
        val task = tasks.create(COLLECT_MOIRA_TRIGGERS_TASK_NAME, CollectMoiraTriggersTask::class.java)
        task.group = "other"
        task.description = "Collect Moira triggers without uploading"
        task.extension = extension
    }

    private fun Project.createExtractTriggersTask(triggersConfiguration: Configuration) {
        val task = tasks.create("extractMoiraTriggers", Copy::class.java)

        val files = triggersConfiguration.files.stream()
                .map { file -> zipTree(file) }
                .toList()

        task.from(files)
        task.into(Paths.get(buildDir.toString(), TRIGGERS_FROM_ARTIFACT_DIR).toString())

        tasks.getByName(UPLOAD_MOIRA_TRIGGERS_TASK_NAME).dependsOn(task)
        tasks.getByName(COLLECT_MOIRA_TRIGGERS_TASK_NAME).dependsOn(task)
    }

    companion object {

        /**
         * Plugin extension name.
         */
        private const val EXTENSION_NAME = "moira"

        /**
         * Plugin source set name (from directory)
         */
        private const val DIR_SOURCE_SET_NAME = "moiraFromDir"

        /**
         * Plugin source set name (from artifact)
         */
        private const val ARTIFACT_SOURCE_SET_NAME = "moiraFromArtifact"

        /**
         * Directory, where triggers from libraries will be saved
         */
        const val TRIGGERS_FROM_ARTIFACT_DIR = "moira"

        /**
         * Configuration, which contains artifacts with triggers
         */
        private const val ARTIFACT_TRIGGERS_CONFIGURATION_NAME = "moiraTriggers"

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
