package ru.yandex.money.gradle.plugins.moira.trigger

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

abstract class AbstractPluginSpec(private val pluginId: String) {

    @get:Rule
    val projectDir = TemporaryFolder()

    lateinit var buildFile: File

    @Before
    fun before() {
        buildFile = projectDir.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id '$pluginId'
            }

            ${repositories()}

        """.trimIndent())

        println("Work directory: ${projectDir.root.absolutePath}")
    }

    protected abstract fun repositories(): String

    fun runTasksSuccessfully(vararg tasks: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments(tasks.toList())
        .withPluginClasspath()
        .forwardOutput()
        .withDebug(true)
        .build()
}
