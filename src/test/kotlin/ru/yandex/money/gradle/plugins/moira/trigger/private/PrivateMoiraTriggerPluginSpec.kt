package ru.yandex.money.gradle.plugins.moira.trigger.private

import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Test
import ru.yandex.money.gradle.plugins.moira.trigger.MoiraTriggerPluginSpec

class PrivateMoiraTriggerPluginSpec : MoiraTriggerPluginSpec("yamoney-moira-trigger-plugin") {

    override val dslArtifactName: String = "ru.yandex.money.tools:yamoney-moira-kotlin-dsl"

    override fun repositories(): String = """ 
        repositories {
            maven { url 'https://nexus.yamoney.ru/repository/thirdparty/' }
            maven { url 'https://nexus.yamoney.ru/repository/central/' }
            maven { url 'https://nexus.yamoney.ru/repository/releases/' }
            maven { url 'https://nexus.yamoney.ru/repository/jcenter.bintray.com/' }
        }
    """.trimIndent()

    @Test
    fun `private - should create triggers`() {
        `should create triggers`()
    }

    @Test
    fun `private - should just prints triggers`() {
        `should just prints triggers`()
    }

    @Test
    fun `should create common triggers`() {
        buildFile.appendText("""

            dependencies {
                moiraFromDirCompile '$dslArtifactName:1.0.3'
                moiraFromArtifactCompile '$dslArtifactName:1.0.3'
                moiraTriggersCompile 'ru.yandex.money.common:yamoney-moira-triggers:1.0.0'
            }

        """.trimIndent())

        val result = runTasksSuccessfully("uploadMoiraTriggers", "--stacktrace", "--info")

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":uploadMoiraTriggers")?.outcome)
        Assert.assertTrue(result.output.contains("Trigger (id=test) has been created successfully"))
        Assert.assertTrue(result.output.contains(Regex("Output of script is empty.*AnomalyErrorsLogsTriggers.kts")))
    }

    @Test
    fun `should prints common triggers`() {
        buildFile.appendText("""

            dependencies {
                moiraFromDirCompile '$dslArtifactName:1.0.3'
                moiraFromArtifactCompile '$dslArtifactName:1.0.3'
                moiraTriggersCompile 'ru.yandex.money.common:yamoney-moira-triggers:1.0.0'
            }

        """.trimIndent())

        val result = runTasksSuccessfully("collectMoiraTriggers", "--stacktrace", "--info")

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":collectMoiraTriggers")?.outcome)
        Assert.assertTrue(result.output.contains("Collected trigger (File: [trigger.kts] Trigger: [Test trigger])"))
        Assert.assertTrue(result.output.contains(Regex("Output of script is empty.*AnomalyErrorsLogsTriggers.kts")))
    }
}
