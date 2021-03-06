/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.event

import chattime.api.Server
import chattime.api.User
import chattime.api.plugin.Plugin

/**
 * Represents a server event (a user joining, for example).
 * Events are fired and handled through the server [EventBus].
 *
 * The [server] property is used in event handling to use server functions
 * and properties.
 *
 * @constructor The primary constructor.
 *
 * @property server the chat server
 */
abstract class Event(
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
 *
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
 * @property user the user
 */
class UserJoinEvent(server: Server,
                    val user: User) : Event(server)

/**
 * This event is fired when a plugin is loading.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @property plugin the plugin
 */
class PluginLoadEvent(server: Server,
                      val plugin: Plugin) : Event(server)

/**
 * This event represents a plugin-to-plugin message.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @property receiverId the message receiver's id
 * @property sender the message sender
 * @property msg the message
 */
class PluginMessageEvent(server: Server,
                         val receiverId: String,
                         val sender: Plugin,
                         val msg: Any) : Event(server)

/**
 * Represents an event type containing an event class.
 * Event type objects hold Java classes of the event types,
 * used when subscribing to events.
 *
 * @constructor The primary constructor.
 * @property eventClass the event class
 */
open class EventType<E : Event> protected constructor(
    val eventClass: Class<E>)
{
    companion object
    {
        /** The event type of [UserJoinEvent]. */
        @JvmField val userJoin = EventType(UserJoinEvent::class.java)

        /** The event type of [PluginMessageEvent]. */
        @JvmField val pluginMessage = EventType(PluginMessageEvent::class.java)

        /** The event type of [PluginLoadEvent]. */
        @JvmField val pluginLoad = EventType(PluginLoadEvent::class.java)

        /** The event type of [MessageEvent]. */
        @JvmField val chatMessage = EventType(MessageEvent::class.java)

        /** The event type of [CommandEvent].*/
        @JvmField val commandCall = EventType(CommandEvent::class.java)
    }
}
