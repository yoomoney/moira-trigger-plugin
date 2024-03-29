package ru.yoomoney.gradle.plugins.moira.trigger.collect

import ru.yoomoney.tech.moira.dsl.triggers.TriggerState.ERROR
import ru.yoomoney.tech.moira.dsl.triggers.TriggerState.OK
import ru.yoomoney.tech.moira.dsl.triggers.trigger
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
