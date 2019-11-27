package ru.yandex.money.gradle.plugins.moira.trigger.public

import org.junit.Test
import ru.yandex.money.gradle.plugins.moira.trigger.MoiraTriggerPluginSpec

class PublicMoiraTriggerPluginSpec : MoiraTriggerPluginSpec("com.yandex.money.tech.moira-trigger-plugin") {

    override val dslArtifactName: String = "com.yandex.money.tech:moira-kotlin-dsl"

    override fun repositories(): String = """ 
        repositories {
            jcenter()
        }
    """.trimIndent()

    @Test
    fun `public - should create triggers`() {
        `should create triggers`()
    }

    @Test
    fun `public - should just prints triggers`() {
        `should just prints triggers`()
    }
}