/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api

import chattime.api.event.EventBus
import chattime.api.features.Commands
import chattime.api.features.Permissions
import chattime.api.plugin.Plugin
import chattime.api.plugin.PluginProperties
import chattime.api.util.Localization

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
     *
     * This user has the id `Server`, all permissions ([Permissions]) by default,
     * and can't be kicked.
     */
    val serverUser: User

    /**
     * The server event bus.
     */
    val eventBus: EventBus

    /* Feature plugins: */

    /**
     * The [Commands] plugin.
     */
    val commandsPlugin: Commands

    /**
     * The [Permissions] plugin.
     */
    val permissionsPlugin: Permissions

    /**
     * The [Localization] object.
     */
    val l10n: Localization

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
     * Gets a [User] by their [User.id].
     * If a user by [id] is not found, throws [IllegalArgumentException].
     *
     * @param id the id
     * @return the user of [id]
     * @throws IllegalArgumentException if user is not found
     */
    fun getUserById(id: String): User
}
