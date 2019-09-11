package ru.yandex.money.gradle.plugins.moira.trigger

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

abstract class AbstractPluginSpec {

    @get:Rule
    val projectDir = TemporaryFolder()

    lateinit var buildFile: File

    @Before
    fun before() {
        buildFile = projectDir.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'yamoney-moira-trigger-plugin'
            }

            repositories {
                maven { url 'https://nexus.yamoney.ru/repository/thirdparty/' }
                maven { url 'https://nexus.yamoney.ru/repository/central/' }
                maven { url 'https://nexus.yamoney.ru/repository/releases/' }
                maven { url 'https://nexus.yamoney.ru/repository/jcenter.bintray.com/' }
            }

        """.trimIndent())

        println("Work directory: ${projectDir.root.absolutePath}")
    }

    fun runTasksSuccessfully(vararg tasks: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments(tasks.toList())
        .withPluginClasspath()
        .forwardOutput()
        .withDebug(true)
        .build()
}
