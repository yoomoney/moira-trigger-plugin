package ru.yandex.money.gradle.plugins.moira.trigger

/**
 * Plugin extension with information about triggers.
 *
 * @author Dmitry Komarov
 * @since 17.06.2019
 */
open class MoiraTriggerExtension {

    /**
     * The name of directory (relative to **projectDir**) where trigger definitions stored in.
     *
     * This property is **required**.
     */
    var dir: String = "moira"

    /**
     * The URL to your Moira service. For example, `moira.your-domain.com`
     *
     * This property is **required**.
     */
    var url: String? = null

    /**
     * The user's login that used for authorization purpose.
     * See [more](https://moira.readthedocs.io/en/latest/installation/security.html) about Moira's security.
     *
     * This property is optional and can be used without [password] property.
     */
    var login: String? = null

    /**
     * The user's password that used for authorization purpose.
     *
     * This property is optional and will be ignored if [login] property is not set.
     */
    var password: String? = null
}
