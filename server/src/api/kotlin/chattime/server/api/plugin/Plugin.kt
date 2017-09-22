/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.plugin

import chattime.server.api.event.*

interface Plugin
{
    val id: String

    val loadOrder: List<LoadOrder>
        get() = emptyList()

    fun load(event: ServerEvent)
    {}

    fun onUserJoin(event: UserEvent)
    {}

    fun onMessageReceived(event: MessageEvent)
    {}

    fun handlePluginMessage(event: PluginMessageEvent)
    {}

    fun onPluginLoaded(event: PluginEvent)
    {}
}

sealed class LoadOrder(val id: String, val isRequired: Boolean)
{
    class Before(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
    class After(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
}
