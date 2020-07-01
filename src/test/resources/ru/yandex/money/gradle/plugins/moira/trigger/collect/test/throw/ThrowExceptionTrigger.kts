package ru.yandex.money.gradle.plugins.moira.trigger.collect.test

import ru.yandex.money.moira.dsl.triggers.trigger

trigger(id = "throw_exception_test_trigger", name = "Throw exception test trigger") {
    description = "Trigger for tests only"
    throw RuntimeException()
}
