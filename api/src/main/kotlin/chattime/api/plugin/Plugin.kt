/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.plugin

import chattime.api.event.PluginLoadEvent

/**
 * A server plugin.
 */
interface Plugin
{
    /**
     * The plugin id.
     */
    val id: String

    /**
     * The plugin load order represented as a list of [LoadOrder] objects.
     * By default this is set to an empty list.
     */
    val loadOrder: List<LoadOrder>
        get() = emptyList()

    /**
     * This function is called when this plugin is constructed.
     * It can be used for subscribing to events or sending plugin messages
     * to other plugins.
     *
     * @param event the loading event
     */
    fun load(event: PluginLoadEvent)
    {}
}
