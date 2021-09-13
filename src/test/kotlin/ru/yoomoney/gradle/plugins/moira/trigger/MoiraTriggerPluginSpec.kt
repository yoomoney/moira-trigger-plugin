package ru.yoomoney.gradle.plugins.moira.trigger

import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Test

class MoiraTriggerPluginSpec : AbstractPluginSpec() {

    @Test
    fun `should create triggers`() {
        buildFile.appendText("""

            dependencies {
                moiraFromDirCompile 'ru.yoomoney.tech:moira-kotlin-dsl:1.4.1-feature-github-SNAPSHOT'
            }

        """.trimIndent())

        val result = runTasksSuccessfully("uploadMoiraTriggers", "--stacktrace", "--info")

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":uploadMoiraTriggers")?.outcome)
        Assert.assertTrue(result.output.contains("Trigger (id=test) has been created successfully"))
    }

    @Test
    fun `should just prints triggers`() {
        buildFile.appendText("""

            dependencies {
                moiraFromDirCompile 'ru.yoomoney.tech:moira-kotlin-dsl:1.4.1-feature-github-SNAPSHOT'
            }

        """.trimIndent())

        val result = runTasksSuccessfully("collectMoiraTriggers", "--stacktrace", "--info")

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":collectMoiraTriggers")?.outcome)
        Assert.assertTrue(result.output.contains("Collected trigger (File: [trigger.kts] Trigger: [Test trigger])"))
        Assert.assertTrue(result.output.contains(Regex("Directory.*moira does not exist. Skip.")))
    }
}