/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.event

import chattime.api.Server
import chattime.api.User

/**
 * This event represents a chat command,
 * and is fired when a command chat message is sent.
 *
 * When canceled, the command will not be called.
 *
 * CommandEvent's [EventType] is [EventType.commandCall].
 *
 * @constructor The primary constructor.
 *
 * @param server the chat server
 * @param commandName the command name
 * @param msg the chat message
 * @param sender the message sender
 */
class CommandEvent(server: Server,
                   /** The command name. */
                   val commandName: String,
                   msg: String,
                   sender: User) : BaseMessageEvent(server, msg, sender)