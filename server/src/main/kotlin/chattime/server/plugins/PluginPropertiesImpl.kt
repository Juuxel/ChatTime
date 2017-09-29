/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.plugins

import chattime.api.plugin.Plugin
import chattime.api.plugin.PluginProperties
import chattime.server.properties
import chattime.server.saveProperties

class PluginPropertiesImpl(plugin: Plugin) : PluginProperties(properties, plugin)
{
    override fun save()
    {
        saveProperties()
    }
}
