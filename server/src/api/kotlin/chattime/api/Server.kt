/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api

import chattime.api.event.EventBus
import chattime.api.features.Features
import chattime.api.plugin.Plugin
import chattime.api.plugin.PluginProperties

/**
 * The chat server which handles all messaging, plugins and users.
 */
interface Server
{
    /**
     * A list of the users currently in the chat.
     */
    val users: List<User>

    /**
     * A list of the loaded plugins.
     */
    val plugins: List<Plugin>

    /**
     * The server console user.
     */
    val serverUser: User

    /**
     * The server event bus.
     */
    val eventBus: EventBus

    /**
     * Sends a message to the users on the server.
     *
     * The [blacklist] and the [whitelist] collections
     * can specify the users more precisely. When the whitelist
     * is not empty, only users on it receive the message. When the blacklist
     * is not empty, every user on it is excluded from receiving the message.
     * The whitelist is prioritized over the blacklist, meaning that
     * if both lists are empty, the whitelist is followed.
     *
     * @param msg the message
     * @param blacklist a collection of users to be excluded from receiving the message
     * @param whitelist a collection of users to be whitelisted for receiving the message
     */
    fun sendMessage(msg: String, blacklist: Collection<User> = emptyList(),
                    whitelist: Collection<User> = emptyList())

    /**
     * Receives a message from a user and forwards it to other users.
     *
     * The message will also be forwarded to the sender if the users
     * [User.isEchoingEnabled] property is set to `true`.
     *
     * @param msg the message
     * @param sender the message sender
     */
    fun forwardMessageFromUser(msg: String, sender: User)

    /**
     * Adds a user to the chat.
     *
     * @param user the user
     */
    fun addUser(user: User)

    /**
     * Gets a [PluginProperties] instance for the [plugin].
     * If a properties instance is not found, a new one will be created, stored
     * and returned by this function.
     *
     * @param plugin the plugin
     * @return the plugin properties instance for [plugin]
     */
    fun getPluginProperties(plugin: Plugin): PluginProperties

    /**
     * Gets a plugin for a [Features] id.
     *
     * @param feature the feature id
     * @return a feature plugin
     */
    fun <P : Plugin> getFeaturePlugin(feature: Features<P>): P
}
