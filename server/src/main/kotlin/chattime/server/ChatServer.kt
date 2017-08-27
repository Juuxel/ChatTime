package chattime.server

import chattime.common.formatMessage
import chattime.server.event.*
import chattime.server.plugins.AttributesPlugin
import chattime.server.plugins.CommandPlugin
import chattime.server.plugins.LoadOrder
import chattime.server.plugins.Plugin

class ChatServer : User
{
    private val threads: ArrayList<ConnectionThread> = ArrayList()
    private val users: ArrayList<User> = arrayListOf(this)
    private val mPlugins: ArrayList<Plugin> = ArrayList()
    private val pluginLoadList: ArrayList<Plugin> = ArrayList()

    val plugins: List<Plugin>
        get() = mPlugins

    // User stuff //

    override val id = "Server"
    override var name = "Server"
    override var isCliUser
        get() = true
        set(value)
        { /* Fake variable :^D */ }

    init
    {
        addPlugin(CommandPlugin())
        addPlugin(AttributesPlugin())
    }


    fun addThread(thread: ConnectionThread)
    {
        threads.add(thread)
        users.add(thread)

        mPlugins.forEach { it.onUserJoin(UserEvent(this, thread)) }

        pushMessage("${thread.name} joined the chat room")
    }

    fun addPlugin(plugin: Plugin) = pluginLoadList.add(plugin)

    internal fun loadPlugins()
    {
        fun doesLoadOrderConflict(p1: Plugin, p2: Plugin): Boolean
        {
            // Before
            if (p1.loadOrder.any { it is LoadOrder.Before && it.id == p2.id} &&
                p2.loadOrder.any { it is LoadOrder.Before && it.id == p1.id})
                return true

            // After
            if (p1.loadOrder.any { it is LoadOrder.After && it.id == p2.id} &&
                p2.loadOrder.any { it is LoadOrder.After && it.id == p1.id})
                return true

            return false
        }

        val sortedLoadList = pluginLoadList.sortedWith(Comparator { p1, p2 ->
            if (doesLoadOrderConflict(p1, p2))
            {
                println("Plugin load orders conflict: ${p1.id}, ${p2.id}")
                pluginLoadList.removeAll(listOf(p1, p2))
            }

            if (p1.loadOrder.any { it is LoadOrder.Before && it.id == p2.id } ||
                p2.loadOrder.any { it is LoadOrder.After && it.id == p1.id })
                return@Comparator -1
            else if (p1.loadOrder.any { it is LoadOrder.After && it.id == p2.id } ||
                p2.loadOrder.any { it is LoadOrder.Before && it.id == p1.id })
                return@Comparator 1

            return@Comparator 0
        })

        mainPlugins@ for (plugin in sortedLoadList)
        {
            for (requiredPlugin in plugin.loadOrder.filter { it.isRequired })
            {
                if (!sortedLoadList.any { it.id == requiredPlugin.id })
                {
                    println("Required plugin ${requiredPlugin.id} for ${plugin.id} not found, skipping loading...")
                    continue@mainPlugins
                }
            }

            mPlugins.add(plugin)
            plugin.load(ServerEvent(this))
            plugins.filter { it != plugin }.forEach {
                it.onPluginLoaded(PluginEvent(this, plugin))
            }
        }
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

    @Synchronized
    fun receiveAndPushMessage(msg: String, user: User)
    {
        val event = MessageEvent(this, msg, user)

        mPlugins.forEach { it.onMessageReceived(event) }

        val formattedMessage = "/${user.name}/ ${event.msg}"

        pushMessage(formattedMessage,
                    blacklist = if (user.isCliUser) listOf(user) else emptyList())
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
