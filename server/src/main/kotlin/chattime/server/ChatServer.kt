/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

import chattime.common.formatMessage
import chattime.server.api.Server
import chattime.server.api.User
import chattime.server.api.event.MessageEvent
import chattime.server.api.event.PluginMessageEvent
import chattime.server.api.event.UserEvent
import chattime.server.api.plugin.Plugin
import chattime.server.api.plugin.PluginProperties
import chattime.server.plugins.*
import kotlin.collections.HashMap

class ChatServer : Server, User
{
    private val threads: ArrayList<ConnectionThread> = ArrayList()
    private val mutUsers: ArrayList<User> = arrayListOf(this)
    internal val pluginLoader = PluginLoader(this)

    override val plugins: List<Plugin>
        get() = pluginLoader.plugins

    override val users: List<User>
        get() = mutUsers

    override val serverUser = this

    private val pluginPropertiesMap: HashMap<Plugin, PluginProperties> = HashMap()

    // User stuff //

    override val id = "Server"
    override var name = "Server"
    override var isEchoingEnabled
        get() = false
        set(value) = Unit // Fake variable :^D

    init
    {
        pluginLoader.addPlugin(CommandPlugin())
        pluginLoader.addPlugin(AttributesPlugin())
    }

    override fun addUser(user: User)
    {
        mutUsers.add(user)

        plugins.forEach { it.onUserJoin(UserEvent(this, user)) }

        pushMessage("${user.name} joined the chat room")
    }

    fun addThread(thread: ConnectionThread)
    {
        threads.add(thread)
        addUser(thread)
    }

    override fun sendPluginMessage(pluginId: String, sender: Plugin, msg: Any)
    {
        plugins.filter { it.id == pluginId }.forEach {
            it.handlePluginMessage(
                PluginMessageEvent(
                    this,
                    sender,
                    msg
                )
            )
        }
    }

    override fun getPluginProperties(plugin: Plugin): PluginProperties
        = pluginPropertiesMap.getOrPut(plugin, { PluginProperties(properties, plugin) })

    @Synchronized
    override fun forwardMessageFromUser(msg: String, sender: User)
    {
        val event = MessageEvent(this, msg, sender)

        plugins.forEach { it.onMessageReceived(event) }

        if (event.isCanceled) return

        val formattedMessage = "/${sender.name}/ ${event.msg}"

        pushMessage(formattedMessage,
                    blacklist = if (sender.isEchoingEnabled) emptyList() else listOf(sender))
    }

    @Synchronized
    override fun pushMessage(msg: String,
                    blacklist: Collection<User>,
                    whitelist: Collection<User>)
    {
        if (whitelist.isEmpty())
            users.filterNot { blacklist.contains(it) }.forEach { it.sendMessage(msg) }
        else
            users.filter { whitelist.contains(it) }.forEach { it.sendMessage(msg) }
    }

    override fun sendMessage(msg: String)
    {
        println(formatMessage(msg))
    }
}
