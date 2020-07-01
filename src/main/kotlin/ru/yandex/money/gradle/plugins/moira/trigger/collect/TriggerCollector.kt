package ru.yandex.money.gradle.plugins.moira.trigger.collect

import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import ru.yandex.money.moira.client.triggers.Trigger
import java.io.File
import java.nio.file.Files
import javax.script.ScriptEngineManager

/**
 * Collector that collects triggers information from their DSL definitions. You can provide some classpath files for
 * compiling and running scripts.
 *
 * Note that single trigger and triggers list definitions are both supported.
 */
class TriggerCollector(classPathFiles: Collection<File>) : FilesCollector<List<Trigger>> {

    private val kotlinEngine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223JvmLocalScriptEngine

    init {
        (kotlinEngine.templateClasspath as MutableList<File>).addAll(classPathFiles)
    }

    /**
     * This collector supports only Kotlin script files (with `.kts` extension).
     */
    override fun isSupported(file: File): Boolean = file.extension == "kts"

    @Suppress("TooGenericExceptionThrown")
    override fun collect(file: File): List<Trigger> {
        val script = String(Files.readAllBytes(file.toPath()), Charsets.UTF_8)
        val json = kotlinEngine.eval(script) as String

        if (json.isEmpty()) {
            log.lifecycle("Output of script is empty, script={}", file)
            return emptyList()
        }

        val tokener = JSONTokener(json)
        val value = tokener.nextValue()

        return when (value) {
            is JSONArray -> value.map { Trigger(it as JSONObject) }
            is JSONObject -> listOf(Trigger(value))
            else -> throw RuntimeException("Unknown trigger generating result: $value")
        }
    }

    companion object {

        private val log = Logging.getLogger(TriggerCollector::class.java)
    }
}
