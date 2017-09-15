/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

import chattime.common.formatMessage
import chattime.server.event.*
import chattime.server.plugins.*
import kotlin.collections.HashMap

class ChatServer : User
{
    private val threads: ArrayList<ConnectionThread> = ArrayList()
    private val users: ArrayList<User> = arrayListOf(this)
    internal val pluginLoader = PluginLoader(this)

    val plugins: List<Plugin>
        get() = pluginLoader.plugins

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

    fun addThread(thread: ConnectionThread)
    {
        threads.add(thread)
        users.add(thread)

        plugins.forEach { it.onUserJoin(UserEvent(this, thread)) }

        pushMessage("${thread.name} joined the chat room")
    }

    fun sendPluginMessage(id: String, sender: Plugin, msg: Any)
    {
        plugins.filter { it.id == id }.forEach {
            it.handlePluginMessage(
                PluginMessageEvent(
                    this,
                    sender,
                    msg
                )
            )
        }
    }

    fun getPluginProperties(plugin: Plugin): PluginProperties
        = pluginPropertiesMap.getOrPut(plugin, { PluginProperties(properties, plugin) })

    @Synchronized
    fun receiveAndPushMessage(msg: String, user: User)
    {
        val event = MessageEvent(this, msg, user)

        plugins.forEach { it.onMessageReceived(event) }

        if (event.isCanceled) return

        val formattedMessage = "/${user.name}/ ${event.msg}"

        pushMessage(formattedMessage,
                    blacklist = if (user.isEchoingEnabled) emptyList() else listOf(user))
    }

    @Synchronized
    fun pushMessage(msg: String,
                    blacklist: Collection<User> = emptyList(),
                    whitelist: Collection<User> = emptyList())
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
