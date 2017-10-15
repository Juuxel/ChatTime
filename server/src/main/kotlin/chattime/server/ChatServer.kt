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
import chattime.api.plugin.Plugin
import chattime.api.plugin.PluginProperties
import chattime.server.plugins.*
import kotlin.collections.HashMap

class ChatServer : Server
{
    private val mutUsers: ArrayList<User> = arrayListOf(ServerUser)
    private val pluginPropertiesMap: HashMap<Plugin, PluginProperties> = HashMap()
    internal val pluginLoader = PluginLoader(this)

    override val plugins: List<Plugin>
        get() = pluginLoader.plugins

    override val users: List<User>
        get() = mutUsers

    override val serverUser = ServerUser

    override val eventBus = EventBusImpl()

    override val commandsPlugin = CommandPlugin()

    init
    {
        pluginLoader.addPlugin(commandsPlugin)
        pluginLoader.addPlugin(AttributesPlugin())
        pluginLoader.addPlugin(PermissionsPlugin())
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

        val formattedMessage = "${sender.name}: ${event.msg}"

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

    override fun getUserById(id: String): User
    {
        val anyUsers = users.any { it.id == id }

        if (anyUsers)
            return users.first { it.id == id }
        else
            throw IllegalArgumentException("No user with id '$id'")
    }

    object ServerUser : User
    {
        override val id = "Server"
        override var name = "Server"

        override var isEchoingEnabled: Boolean
            get() = false
            set(value) = Unit

        override fun sendMessage(msg: String)
        {
            println(formatMessage(msg))
        }
    }
}
