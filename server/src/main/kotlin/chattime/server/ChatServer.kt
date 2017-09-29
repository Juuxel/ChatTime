/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

import chattime.common.formatMessage
import chattime.api.Server
import chattime.api.User
import chattime.api.event.MessageEvent
import chattime.api.event.UserJoinEvent
import chattime.api.features.Features
import chattime.api.plugin.Plugin
import chattime.api.plugin.PluginProperties
import chattime.server.plugins.*
import kotlin.collections.HashMap

class ChatServer : Server, User
{
    private val mutUsers: ArrayList<User> = arrayListOf(this)
    private val pluginPropertiesMap: HashMap<Plugin, PluginProperties> = HashMap()
    internal val pluginLoader = PluginLoader(this)
    private val commandPlugin = CommandPlugin()

    override val plugins: List<Plugin>
        get() = pluginLoader.plugins

    override val users: List<User>
        get() = mutUsers

    override val serverUser = this

    override val eventBus = EventBusImpl()

    // User stuff //

    override val id = "Server"
    override var name = "Server"
    override var isEchoingEnabled
        get() = false
        set(value) = Unit // Fake variable :^D

    init
    {
        pluginLoader.addPlugin(commandPlugin)
        pluginLoader.addPlugin(AttributesPlugin())
    }

    override fun addUser(user: User)
    {
        mutUsers += user

        eventBus.post(UserJoinEvent(this, user))

        sendMessage("${user.name} joined the chat room")
    }

    override fun getPluginProperties(plugin: Plugin): PluginProperties
        = pluginPropertiesMap.getOrPut(plugin, { PluginPropertiesImpl(plugin) })

    @Synchronized
    override fun forwardMessageFromUser(msg: String, sender: User)
    {
        val event = MessageEvent(this, msg, sender)

        eventBus.post(event)

        if (event.isCanceled) return

        val formattedMessage = "/${sender.name}/ ${event.msg}"

        sendMessage(formattedMessage,
                    blacklist = if (sender.isEchoingEnabled) emptyList() else listOf(sender))
    }

    @Synchronized
    override fun sendMessage(msg: String,
                             blacklist: Collection<User>,
                             whitelist: Collection<User>)
    {
        if (whitelist.isEmpty())
            users.filterNot { blacklist.contains(it) }.forEach { it.sendMessage(msg) }
        else
            users.filter { whitelist.contains(it) }.forEach { it.sendMessage(msg) }
    }

    @Suppress("unchecked_cast")
    override fun <P : Plugin> getFeaturePlugin(feature: Features<P>): P
        = when (feature)
        {
            Features.commands -> commandPlugin as P
            else -> throw IllegalArgumentException("Unknown feature $feature")
        }

    override fun sendMessage(msg: String)
    {
        println(formatMessage(msg))
    }
}
