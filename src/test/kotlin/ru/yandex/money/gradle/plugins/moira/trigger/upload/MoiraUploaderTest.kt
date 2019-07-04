package ru.yandex.money.gradle.plugins.moira.trigger.upload

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.yandex.money.moira.client.triggers.Trigger
import ru.yandex.money.moira.client.triggers.TriggerType
import ru.yandex.money.moira.client.triggers.expression.AdvancedExpression
import ru.yandex.money.moira.client.triggers.expression.SimpleExpression
import java.io.IOException
import java.net.ServerSocket
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MoiraUploaderTest {

    private val moiraPort: Int by lazy {
        try {
            ServerSocket(0).use { socket -> socket.localPort }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private var succeed = true
    private var triggers = emptyList<Trigger>()
    private var id: String = ""

    private val requests = mutableListOf<String>()

    inner class TestHandler : AbstractHandler() {

        override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
            val result = if (request.method == "GET") {
                // запрос на получение списка всех триггеров
                val json = JSONObject()
                json.put("list", triggers.map { it.toJson() })
                json
            } else {
                // запрос на добавление/обновление триггера
                if (succeed) {
                    response.status = HttpServletResponse.SC_OK
                } else {
                    response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                }

                val json = JSONObject()
                json.put("id", id)
                json
            }

            response.writer.print(result.toString())
            baseRequest.isHandled = true

            requests += "${baseRequest.method}${baseRequest.httpURI.path} ${response.status}"
        }
    }

    private fun setupJettyServer() {
        val server = Server(moiraPort)
        server.stopAtShutdown = true
        val handler = TestHandler()
        server.handler = handler
        server.start()
    }

    @Before
    fun setup() {
        setupJettyServer()
    }

    @Test
    fun `should just return when no triggers to upload`() {
        // given
        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(emptyList())

        // then
        assertTrue("$requests", requests.isEmpty())
    }

    @Test
    fun `should create new trigger when no existing trigger with same id`() {
        // given
        id = "test_trigger"

        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(listOf(
            Trigger(
                id = "test_trigger",
                name = "Test trigger",
                tags = listOf("test"),
                targets = listOf("sumSeries(*.metric.process.succeeded.count)"),
                triggerExpression = SimpleExpression(
                    triggerType = TriggerType.RISING,
                    warnValue = null,
                    errorValue = 20.0
                )
            )
        ))

        // then
        assertEquals("GET/trigger 200", requests[0])
        assertEquals("PUT/trigger 200", requests[1])
    }

    @Test
    fun `should update existing trigger when found existing trigger with same id`() {
        // given
        triggers = listOf(
            Trigger(
                id = "test_trigger",
                name = "Test trigger",
                tags = listOf("test", "obsolete"),
                targets = listOf("sumSeries(*.metric.process.*.count)"),
                triggerExpression = AdvancedExpression("OK")
            )
        )
        id = "test_trigger"

        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(listOf(
            Trigger(
                id = "test_trigger",
                name = "Test trigger",
                tags = listOf("test"),
                targets = listOf("sumSeries(*.metric.process.succeeded.count)"),
                triggerExpression = SimpleExpression(
                    triggerType = TriggerType.RISING,
                    warnValue = null,
                    errorValue = 20.0
                )
            )
        ))

        // then
        assertEquals("GET/trigger 200", requests[0])
        assertEquals("PUT/trigger/test_trigger 200", requests[1])
    }

    @Test
    fun `should create new trigger when no existing trigger with same name`() {
        // given
        id = UUID.randomUUID().toString()

        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(listOf(
            Trigger(
                name = "Test trigger",
                tags = listOf("test"),
                targets = listOf("sumSeries(*.metric.process.succeeded.count)"),
                triggerExpression = SimpleExpression(
                    triggerType = TriggerType.RISING,
                    warnValue = null,
                    errorValue = 20.0
                )
            )
        ))

        // then
        assertEquals("GET/trigger 200", requests[0])
        assertEquals("PUT/trigger 200", requests[1])
    }

    @Test
    fun `should update existing trigger when found existing trigger with same name`() {
        // given
        triggers = listOf(
            Trigger(
                id = UUID.randomUUID().toString(),
                name = "Test trigger",
                tags = listOf("test", "obsolete"),
                targets = listOf("sumSeries(*.metric.process.*.count)"),
                triggerExpression = AdvancedExpression("OK")
            )
        )
        id = triggers[0].id!!

        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(listOf(
            Trigger(
                name = "Test trigger",
                tags = listOf("test"),
                targets = listOf("sumSeries(*.metric.process.succeeded.count)"),
                triggerExpression = SimpleExpression(
                    triggerType = TriggerType.RISING,
                    warnValue = null,
                    errorValue = 20.0
                )
            )
        ))

        // then
        assertEquals("GET/trigger 200", requests[0])
        assertEquals("PUT/trigger/$id 200", requests[1])
    }

    @Test
    fun `should just return when update of existing trigger has been unsuccessful`() {
        // given
        triggers = listOf(
            Trigger(
                id = UUID.randomUUID().toString(),
                name = "Test trigger",
                tags = listOf("test", "obsolete"),
                targets = listOf("sumSeries(*.metric.process.*.count)"),
                triggerExpression = AdvancedExpression("OK")
            )
        )
        id = triggers[0].id!!
        succeed = false

        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(listOf(
            Trigger(
                name = "Test trigger",
                tags = listOf("test"),
                targets = listOf("sumSeries(*.metric.process.succeeded.count)"),
                triggerExpression = SimpleExpression(
                    triggerType = TriggerType.RISING,
                    warnValue = null,
                    errorValue = 20.0
                )
            )
        ))

        // then
        assertEquals("GET/trigger 200", requests[0])
        assertEquals("PUT/trigger/$id 500", requests[1])
    }

    @Test
    fun `should just return when creating new trigger throws an exception`() {
        // given
        succeed = false

        val uploader = MoiraUploader(url = "http://localhost:$moiraPort")

        // when
        uploader.upload(listOf(
            Trigger(
                id = "test_trigger",
                name = "Test trigger",
                tags = listOf("test"),
                targets = listOf("sumSeries(*.metric.process.succeeded.count)"),
                triggerExpression = SimpleExpression(
                    triggerType = TriggerType.RISING,
                    warnValue = null,
                    errorValue = 20.0
                )
            )
        ))

        // then
        assertEquals("GET/trigger 200", requests[0])
        assertEquals("PUT/trigger 500", requests[1])
    }
}
