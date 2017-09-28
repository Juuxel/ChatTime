/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.event

import chattime.server.api.Server
import chattime.server.api.User

/**
 * An event which represents a chat message.
 *
 * MessageEvent's [EventType] is [EventType.chatMessage].
 *
 * @param server the chat server
 * @param msg the chat message
 * @param sender the message sender
 */
class MessageEvent(server: Server,
                   var msg: String,
                   val sender: User) : CancelableEvent(server)
{
    /**
     * Sends a message ([msg]) to [sender].
     *
     * @see Server.sendMessage
     */
    fun sendMessageToSender(msg: String) = server.sendMessage(msg, whitelist = listOf(sender))
}
