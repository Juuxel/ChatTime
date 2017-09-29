/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api

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
    fun sendMessage(msg: String)
}
