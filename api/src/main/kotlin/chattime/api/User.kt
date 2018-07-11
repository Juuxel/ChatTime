/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api

import chattime.api.net.Packet

/**
 * Represents a user on the server.
 */
interface User
{
    /**
     * The immutable unique user id.
     */
    val id: String

    /**
     * The user name which can be changed by code and commands.
     */
    var name: String

    /**
     * If this is set to true, the messages sent by this user
     * will be sent back. This is useful in clients so that they
     * can display the user's own messages without formatting on
     * the client's side.
     */
    var isEchoingEnabled: Boolean

    /**
     * Sends a message to this user. The message will be displayed
     * on the user's chat client.
     *
     * @param msg the message
     */
    fun sendMessage(msg: Packet.Message)

    /**
     * Removes this user from the chat.
     */
    fun kick()
}
