/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server

import chattime.api.User
import chattime.common.nextInt
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class ConnectionThread(private val client: Socket, private val server: ChatServer) : Runnable, User
{
    override val id = "User-" + newUserId()
    override var name = id
    override var isEchoingEnabled = true
    private val clientIn = Scanner(client.inputStream, Charsets.UTF_8.name())
    private val clientOut = PrintWriter(client.outputStream.bufferedWriter(Charsets.UTF_8), true)

    companion object
    {
        private val userIdPool = ArrayList<String>()
        private val rng = Random()

        /**
         * Generates a four-digit number for a user id (includes leading
         * zeros).
         *
         * @return a number for a user id
         */
        fun newUserId(): String
        {
            var id: String

            do
            {
                val num = rng.nextInt(1..9999)
                id = num.toString().padStart(4, '0')
            } while (userIdPool.contains(id))

            userIdPool += id

            return id
        }
    }

    override fun run()
    {
        try
        {
            do
            {
                val input = clientIn.nextLine()
                server.forwardMessageFromUser(input, sender = this)
            } while (input != null)
        }
        catch (e: Exception)
        {
            server.sendMessage("$name left the chat room")
            client.close() // Close the socket just in case
        }

    }

    override fun sendMessage(msg: String)
    {
        clientOut.println(msg)
    }
}
