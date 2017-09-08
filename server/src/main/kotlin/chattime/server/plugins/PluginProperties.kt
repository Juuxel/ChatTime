/* This file is a part of the ChatTime project.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.plugins

import chattime.server.event.Event
import java.util.*

class PluginProperties(private val serverProperties: Properties,
                       private val plugin: Plugin)
{

    private fun getCustomKey(key: String): String = "plugins.${plugin.id}.$key"

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

    operator fun set(key: String, value: String)
    {
        serverProperties.put(getCustomKey(key), value)
    }

    fun save(event: Event)
    {
        event.server.saveProperties()
    }
}
