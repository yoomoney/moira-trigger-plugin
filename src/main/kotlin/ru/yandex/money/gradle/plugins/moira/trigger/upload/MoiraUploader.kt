package ru.yandex.money.gradle.plugins.moira.trigger.upload

import org.gradle.api.logging.Logging
import ru.yandex.money.moira.client.Moira
import ru.yandex.money.moira.client.http.UserCredentials
import ru.yandex.money.moira.client.settings.MoiraSettings
import ru.yandex.money.moira.client.triggers.Trigger

/**
 * Uploader for Moira triggers. For configuration, see [MoiraSettings].
 */
class MoiraUploader(url: String, login: String? = null, password: String? = null) {

    private val moira = Moira(settings = MoiraSettings(
        baseUrl = url,
        login = login,
        credentials = if (login != null && password != null) UserCredentials(login, password) else null
    ))

    /**
     * Uploads triggers to Moira. If trigger with same name already exists in Moira, so, this trigger will be updated.
     */
    @Suppress("TooGenericExceptionCaught")
    fun upload(triggers: List<Trigger>) {
        if (triggers.isEmpty()) {
            log.lifecycle("No triggers to upload")
            return
        }

        val existingTriggers = moira.triggers.fetchAll()

        triggers.forEach {
            try {
                uploadTrigger(it, existingTriggers)
            } catch (e: Exception) {
                log.error("Failed to upload trigger: {}", it, e)
            }
        }
    }

    private fun uploadTrigger(trigger: Trigger, existingTriggers: List<Trigger>) {
        for (existingTrigger in existingTriggers) {
            if (check(existingTrigger, trigger)) {
                log.lifecycle("Trigger with same ID or name already exists: id={}, name={}",
                    existingTrigger.id, existingTrigger.name)
                val triggerToUpdate = trigger.copy(id = existingTrigger.id)
                if (moira.triggers.update(triggerToUpdate)) {
                    log.lifecycle("Trigger (id={}) has been updated successfully", triggerToUpdate.id)
                } else {
                    log.warn("Trigger (id={}) has not been updated", triggerToUpdate.id)
                }
                return
            }
        }

        val id = moira.triggers.create(trigger)
        log.lifecycle("Trigger (id={}) has been created successfully", id)
    }

    private fun check(existingTrigger: Trigger, creatingTrigger: Trigger): Boolean = if (creatingTrigger.id != null) {
        existingTrigger.id == creatingTrigger.id
    } else {
        existingTrigger.name == creatingTrigger.name
    }

    companion object {

        private val log = Logging.getLogger(MoiraUploader::class.java)
    }
}
