package ru.yoomoney.gradle.plugins.moira.trigger

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class AbstractPluginSpec {

    @get:Rule
    val projectDir = TemporaryFolder()

    lateinit var buildFile: File

    private val moiraPort: Int by lazy {
        try {
            ServerSocket(0).use { socket -> socket.localPort }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private inner class HelloHandler : AbstractHandler() {
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

    @Before
    fun before() {
        buildFile = projectDir.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'ru.yoomoney.gradle.plugins.moira-trigger-plugin'
            }

            repositories {
                jcenter()
            }

            moira {
                url = 'http://localhost:$moiraPort'
            }
        """.trimIndent())

        println("Work directory: ${projectDir.root.absolutePath}")

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
    }

    fun runTasksSuccessfully(vararg tasks: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments(tasks.toList())
        .withPluginClasspath()
        .forwardOutput()
        .withDebug(true)
        .build()

    private fun setupJettyServer() {
        val server = Server(moiraPort)
        server.stopAtShutdown = true
        val handler = HelloHandler()
        server.handler = handler
        server.start()
    }
}
