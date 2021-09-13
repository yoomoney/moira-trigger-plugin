package ru.yoomoney.gradle.plugins.moira.trigger.collect.test.`throw`

import ru.yoomoney.tech.moira.dsl.triggers.trigger

trigger(id = "throw_exception_test_trigger", name = "Throw exception test trigger") {
    description = "Trigger for tests only"
    throw RuntimeException()
}
