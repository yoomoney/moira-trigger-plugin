package ru.yandex.money.gradle.plugins.moira.trigger

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import java.io.IOException
import java.net.ServerSocket
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class MoiraTriggerPluginSpec(pluginId: String) : AbstractPluginSpec(pluginId) {

    private val moiraPort: Int by lazy {
        try {
            ServerSocket(0).use { socket -> socket.localPort }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    inner class HelloHandler : AbstractHandler() {
        @Throws(IOException::class, ServletException::class)
        override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
            val result = if (request.method == "GET") {
                // запрос на получение списка всех триггеров
                """{"list": []}"""
            } else {
                // запрос на добавление триггера
                """{"id": "test"}"""
            }

            response.status = HttpServletResponse.SC_OK
            response.writer.print(result)
            baseRequest.isHandled = true
        }
    }

    private fun setupJettyServer() {
        val server = Server(moiraPort)
        server.stopAtShutdown = true
        val handler = HelloHandler()
        server.handler = handler
        server.start()
    }

    @Before
    fun setup() {
        setupJettyServer()

        projectDir.newFolder("moira")
        projectDir.newFile("moira/trigger.kts").writeText("""
            import ru.yandex.money.moira.dsl.triggers.TriggerState.ERROR
            import ru.yandex.money.moira.dsl.triggers.TriggerState.OK
            import ru.yandex.money.moira.dsl.triggers.trigger
            import java.time.Duration

            trigger(id = "test_trigger", name = "Test trigger") {
                description = "Trigger for tests only"

                val t1 by target("sumSeries(*.metric.process.succeeded.count)")

                tags += "test"

                ttl {
                    ttl = Duration.ofMinutes(2)
                    state = ERROR
                }

                expression {
                    advanced {
                        OK.state
                    }
                }
            }
        """.trimIndent())

        buildFile.appendText("""
            moira {
                url = 'http://localhost:$moiraPort'
            }
        """.trimIndent())
    }

    protected abstract val dslArtifactName: String

    fun `should create triggers`() {
        buildFile.appendText("""

            dependencies {
                moiraFromDirCompile '$dslArtifactName:1.0.3'
            }

        """.trimIndent())

        val result = runTasksSuccessfully("uploadMoiraTriggers", "--stacktrace", "--info")

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":uploadMoiraTriggers")?.outcome)
        Assert.assertTrue(result.output.contains("Trigger (id=test) has been created successfully"))
    }

    fun `should just prints triggers`() {
        buildFile.appendText("""

            dependencies {
                moiraFromDirCompile '$dslArtifactName:1.0.3'
            }

        """.trimIndent())

        val result = runTasksSuccessfully("collectMoiraTriggers", "--stacktrace", "--info")

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":collectMoiraTriggers")?.outcome)
        Assert.assertTrue(result.output.contains("Collected trigger (File: [trigger.kts] Trigger: [Test trigger])"))
        Assert.assertTrue(result.output.contains(Regex("Directory.*moira does not exist. Skip.")))
    }
}