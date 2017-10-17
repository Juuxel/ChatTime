/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
