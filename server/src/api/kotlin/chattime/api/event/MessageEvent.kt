/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api.event

import chattime.api.Server
import chattime.api.User

/**
 * This event represents a chat message.
 *
 * MessageEvent's [EventType] is [EventType.chatMessage].
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @param msg the chat message
 * @param sender the message sender
 */
class MessageEvent(server: Server,
                   /** The chat message. */
                   var msg: String,
                   /** The message sender. */
                   val sender: User) : CancelableEvent(server)
{
    /**
     * Sends a message ([msg]) to [sender].
     *
     * @see Server.sendMessage
     */
    fun sendMessageToSender(msg: String) = server.sendMessage(msg, whitelist = listOf(sender))
}
