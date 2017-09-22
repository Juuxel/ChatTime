/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.event

import chattime.server.api.Server
import chattime.server.api.User
import chattime.server.api.plugin.Plugin

abstract class Event(val server: Server)
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

class ServerEvent(server: Server) : Event(server)

class MessageEvent(server: Server,
                   var msg: String,
                   val sender: User) : Event(server)
{
    override val isCancelable = true

    fun sendMessageToSender(msg: String) = server.sendMessage(msg, whitelist = listOf(sender))
}

class UserEvent(server: Server, val user: User) : Event(server)

class PluginEvent(server: Server, val plugin: Plugin) : Event(server)

class PluginMessageEvent(server: Server,
                         val sender: Plugin,
                         val msg: Any) : Event(server)
