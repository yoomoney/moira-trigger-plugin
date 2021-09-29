package ru.yoomoney.gradle.plugins.moira.trigger.collect

/**
 * The information about collected item along with the name of the file from which this item was collected.
 */
data class ItemWithFileName<T>(val fileName: String, val item: T)
