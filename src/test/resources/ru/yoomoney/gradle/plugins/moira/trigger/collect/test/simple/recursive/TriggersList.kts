package ru.yoomoney.gradle.plugins.moira.trigger.collect.test.simple.recursive

import ru.yoomoney.tech.moira.dsl.triggers.TriggerState.ERROR
import ru.yoomoney.tech.moira.dsl.triggers.TriggerState.OK
import ru.yoomoney.tech.moira.dsl.triggers.triggers
import java.time.Duration

triggers {
    trigger(id = "test_trigger_1", name = "Test trigger 1 (succeeded)") {

        description = "succeeded metrics"

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
    trigger(id = "test_trigger_2", name = "Test trigger 2 (failed)") {

        description = "failed metrics"

        val t1 by target("sumSeries(*.metric.process.failed.count)")

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
}
