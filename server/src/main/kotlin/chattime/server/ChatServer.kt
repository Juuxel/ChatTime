/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server

import chattime.common.formatMessage
import chattime.api.Server
import chattime.api.User
import chattime.api.event.MessageEvent
import chattime.api.event.UserJoinEvent
import chattime.api.net.Packet
import chattime.api.plugin.Plugin
import chattime.api.plugin.PluginProperties
import chattime.server.plugins.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.HashMap

class ChatServer : Server
{
    private val pluginPropertiesMap: HashMap<Plugin, PluginProperties> = HashMap()
    internal val pluginLoader = PluginLoader(this)
    internal val mutableUsers = arrayListOf<User>(ServerUser)

    override val plugins: List<Plugin>
        get() = pluginLoader.plugins

    override val users: List<User>
        get() = mutableUsers

    override val serverUser = ServerUser

    override val eventBus = EventBusImpl()

    override val l10n = L10n

    // Feature plugins

    override val commandsPlugin = CommandPlugin()
    override val permissionsPlugin = PermissionsPlugin()

    init
    {
        pluginLoader.addPlugin(commandsPlugin)
        pluginLoader.addPlugin(permissionsPlugin)
        pluginLoader.addPlugin(AttributesPlugin())
    }

    override fun addUser(user: User)
    {
        mutableUsers += user
        eventBus.post(UserJoinEvent(this, user))
        sendMessage(L10n["user.join", user.name])
    }

    override fun getPluginProperties(plugin: Plugin): PluginProperties
        = pluginPropertiesMap.getOrPut(plugin) { PluginPropertiesImpl(plugin) }

    @Synchronized
    override fun forwardMessageFromUser(msg: Packet.Message, sender: User)
    {
        val event = MessageEvent(this, Packet.Message(sender.id, msg.message), sender)

        eventBus.post(event)
        if (event.isCanceled) return

        sendMessage(event.msg, blacklist = if (sender.isEchoingEnabled) emptyList() else listOf(sender))
    }

    @Synchronized
    override fun sendMessage(msg: Packet.Message,
                             blacklist: Collection<User>,
                             whitelist: Collection<User>)
    {
        if (whitelist.isEmpty())
            users.filterNot { blacklist.contains(it) }.forEach { it.sendMessage(msg) }
        else
            users.filter { whitelist.contains(it) }.forEach { it.sendMessage(msg) }
    }

    @Synchronized
    override fun sendMessage(msg: String,
                             blacklist: Collection<User>,
                             whitelist: Collection<User>)
    {
        sendMessage(Packet.Message("", msg), blacklist, whitelist)
    }

    override fun getUserById(id: String): User
    {
        val anyUsers = users.any { it.id == id }

        if (anyUsers)
            return users.first { it.id == id }
        else
            throw IllegalArgumentException(L10n["error.userNotFound", id])
    }

    object ServerUser : User
    {
        override val id = "Server"
        override var name = "Server"

        override var isEchoingEnabled: Boolean
            get() = false
            set(_) = Unit

        override fun sendMessage(msg: Packet.Message)
        {
            println(formatMessage(msg))
        }

        override fun kick()
        {}
    }
}
