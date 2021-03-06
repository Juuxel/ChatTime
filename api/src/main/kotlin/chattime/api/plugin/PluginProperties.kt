/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.plugin

import java.util.*

/**
 * A property map for plugins. Instances of this class can
 * be obtained from the server with [chattime.api.Server.getPluginProperties].
 *
 * @constructor The primary constructor.
 *
 * @param serverProperties the main properties object
 * @param plugin the owner plugin
 */
abstract class PluginProperties(private val serverProperties: Properties,
                                private val plugin: Plugin)
{
    private fun getCustomKey(key: String): String = "plugins.${plugin.id}.$key"

    /**
     * Gets the property value of [key].
     *
     * If the value is null, returns `"null"` unless [default]
     * is set to a non-null value. If [default] is set, the value of [key]
     * is set to [default] and the function returns [default].
     *
     * @param key the property key
     * @param default the default property value
     * @return the value of [key]
     */
    operator fun get(key: String, default: String? = null): String
    {
        val value = serverProperties.getProperty(getCustomKey(key))

        if (value == null)
        {
            if (default != null)
            {
                set(key, default)
                return default
            }

            return "null"
        }

        return value
    }

    /**
     * Sets a property with [key] to [value].
     *
     * @param key the property key
     * @param value the new value
     */
    operator fun set(key: String, value: String)
    {
        serverProperties.put(getCustomKey(key), value)
    }

    /**
     * Stores the plugin properties in a file.
     */
    abstract fun save()
}
