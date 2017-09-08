package chattime.server.event

import chattime.server.ChatServer
import chattime.server.plugins.Plugin
import chattime.server.User

abstract class Event(val server: ChatServer)
{
    var isCanceled: Boolean = false
        get
        private set

    open val isCancelable: Boolean = false

    fun cancel()
    {
        isCanceled = true
    }
}

class ServerEvent(server: ChatServer) : Event(server)

class MessageEvent(server: ChatServer,
                   var msg: String,
                   val sender: User) : Event(server)
{
    override val isCancelable = true

    fun pushMessageToSender(msg: String) = server.pushMessage(msg, whitelist = listOf(sender))
}

class UserEvent(server: ChatServer, val user: User) : Event(server)

class PluginEvent(server: ChatServer, val plugin: Plugin) : Event(server)

class PluginMessageEvent(server: ChatServer,
                         val sender: Plugin,
                         val msg: Any) : Event(server)
