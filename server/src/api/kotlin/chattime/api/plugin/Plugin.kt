/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api.plugin

import chattime.api.event.PluginLoadEvent
import chattime.server.api.event.*

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

/**
 * Represents an entry in the plugin load order list.
 *
 * @constructor The primary constructor.
 *
 * @param id the other plugin
 * @param isRequired set to true if the other plugin is required
 */
sealed class LoadOrder(
    /** The other plugin. */
    val id: String,
    /** If this is true, the other plugin is required. */
    val isRequired: Boolean)
{
    /**
     * Loads this plugin before the other plugin.
     *
     * @constructor The primary constructor.
     *
     * @param id the other plugin
     * @param isRequired set to true if the other plugin is required
     */
    class Before(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)

    /**
     * Loads this plugin after the other plugin.
     *
     * @constructor The primary constructor.
     *
     * @param id the other plugin
     * @param isRequired set to true if the other plugin is required
     */
    class After(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
}
