/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.event

import chattime.server.api.Server
import chattime.server.api.User
import chattime.server.api.plugin.Plugin

/**
 * Represents a server event (a user joining, for example).
 * Events are fired and handled through the server [EventBus].
 *
 * @param server the chat server
 */
abstract class Event(val server: Server)
{
    /**
     * A property with the server event bus ([Server.eventBus])
     * as its value.
     */
    val eventBus
        get() = server.eventBus
}

/**
 * Represents an event that can be canceled.
 * If an event is canceled, the final action after the event
 * should not be called.
 *
 * @param server the chat server
 */
abstract class CancelableEvent(server: Server) : Event(server)
{
    /**
     * If this is true, the event is canceled.
     */
    var isCanceled: Boolean = false
        get
        private set

    /**
     * Cancels the event by setting [isCanceled] to true.
     * This cannot be reversed.
     */
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

/**
 * Represents an event type containing an event class.
 * Event type objects hold Java classes of the event types,
 * used when subscribing to events.
 *
 * @param eventClass the event class
 */
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
