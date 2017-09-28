/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.event

import chattime.server.api.Server
import chattime.server.api.User
import chattime.server.api.plugin.Plugin

abstract class Event(val server: Server)
{
    val eventBus
        get() = server.eventBus
}

abstract class CancelableEvent(server: Server) : Event(server)
{
    var isCanceled: Boolean = false
        get
        private set

    fun cancel()
    {
        isCanceled = true
    }
}

class UserJoinEvent(server: Server, val user: User) : Event(server)
class PluginLoadEvent(server: Server, val plugin: Plugin) : Event(server)
class PluginMessageEvent(server: Server,
                         val receiverId: String,
                         val sender: Plugin,
                         val msg: Any) : Event(server)

open class EventType<E : Event> protected constructor(val eventClass: Class<E>)
{
    companion object
    {
        val userJoin = EventType(UserJoinEvent::class.java)
        val pluginMessage = EventType(PluginMessageEvent::class.java)
        val pluginLoad = EventType(PluginLoadEvent::class.java)
        val chatMessage = EventType(MessageEvent::class.java)
    }
}
