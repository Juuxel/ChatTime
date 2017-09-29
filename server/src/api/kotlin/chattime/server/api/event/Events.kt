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
 * The [server] property is used in event handling to use server functions
 * and properties.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 */
abstract class Event(
    /** The chat server. */
    val server: Server)
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
 * @constructor The primary constructor.
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

/**
 * This event is fired when a user joins the server.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @param user the user
 */
class UserJoinEvent(server: Server,
                    /** The user who is joining. */
                    val user: User) : Event(server)

/**
 * This event is fired when a plugin is loading.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @param plugin the plugin
 */
class PluginLoadEvent(server: Server,
                      /** The plugin which is loading. */
                      val plugin: Plugin) : Event(server)

/**
 * This event represents a plugin-to-plugin message.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @param receiverId the message receiver
 * @param sender the message sender
 * @param msg the message
 */
class PluginMessageEvent(server: Server,
                         /** The message receiver's id. */
                         val receiverId: String,

                         /** The message sender. */
                         val sender: Plugin,

                         /** The message. */
                         val msg: Any) : Event(server)

/**
 * Represents an event type containing an event class.
 * Event type objects hold Java classes of the event types,
 * used when subscribing to events.
 *
 * @constructor The primary constructor.
 *
 * @param eventClass the event class
 */
open class EventType<E : Event> protected constructor(
    /** The event class. */
    val eventClass: Class<E>)
{
    companion object
    {
        /** The event type of [UserJoinEvent]. */
        val userJoin = EventType(UserJoinEvent::class.java)

        /** The event type of [PluginMessageEvent]. */
        val pluginMessage = EventType(PluginMessageEvent::class.java)

        /** The event type of [PluginLoadEvent]. */
        val pluginLoad = EventType(PluginLoadEvent::class.java)

        /** The event type of [MessageEvent]. */
        val chatMessage = EventType(MessageEvent::class.java)
    }
}
