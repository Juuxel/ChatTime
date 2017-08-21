package chattime.server

import chattime.common.formatMessage
import chattime.server.event.MessageEvent
import chattime.server.event.ServerEvent
import chattime.server.event.UserEvent
import chattime.server.plugins.Plugin

class ChatServer : User
{
    private val threads: ArrayList<ConnectionThread> = ArrayList()
    private val users: ArrayList<User> = arrayListOf(this)
    private val plugins: ArrayList<Plugin> = ArrayList()

    override val id = "Server"
    override var name = "Server"
    override var isCliUser
        get() = true
        set(value) { /* Fake variable :^D */ }

    fun addThread(thread: ConnectionThread)
    {
        threads.add(thread)
        users.add(thread)

        plugins.forEach { it.onUserJoin(UserEvent(this, thread)) }

        pushMessage("${thread.name} joined the chat room")
    }

    fun addPlugin(plugin: Plugin)
    {
        plugins.add(plugin)
        plugin.onPluginLoad(ServerEvent(this))
    }

    @Synchronized fun receiveAndPushMessage(msg: String, user: User)
    {
        val event = MessageEvent(this, msg, user)

        plugins.forEach { it.onMessageReceived(event) }

        val formattedMessage = "/${user.name}/ ${event.msg}"

        pushMessage(formattedMessage,
                    blacklist = if (user.isCliUser) listOf(user) else emptyList())
    }

    @Synchronized fun pushMessage(msg: String,
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
