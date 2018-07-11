/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.event

import chattime.api.Server
import chattime.api.User
import chattime.api.net.Packet

/**
 * This event is a base event for event that represent a chat message.
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @property msg the chat message
 * @property sender the message sender
 */
abstract class BaseMessageEvent(server: Server, var msg: Packet.Message, val sender: User) : CancelableEvent(server)
{
    /**
     * Sends a message ([msg]) to [sender].
     *
     * @see Server.sendMessage
     */
    fun sendMessageToSender(msg: String) = server.sendMessage(msg, whitelist = listOf(sender))
}

/**
 * This event represents a chat message.
 *
 * When canceled, the message will not be sent to other users.
 *
 * MessageEvent's [EventType] is [EventType.chatMessage].
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @param msg the chat message
 * @param sender the message sender
 */
class MessageEvent(server: Server, msg: Packet.Message, sender: User)
    : BaseMessageEvent(server, msg, sender)
