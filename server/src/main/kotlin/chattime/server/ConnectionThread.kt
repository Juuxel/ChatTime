/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server

import chattime.api.User
import chattime.api.net.Packet
import chattime.common.nextInt
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.net.SocketException
import java.util.*

class ConnectionThread(private val client: Socket, private val server: ChatServer) : Runnable, User
{
    override val id = "User-" + newUserId()
    override var name = id
    override var isEchoingEnabled = true
    private val clientIn = DataInputStream(client.getInputStream())
    private val clientOut = DataOutputStream(client.getOutputStream())

    companion object
    {
        private val userIdPool = ArrayList<String>()
        private val rng = Random()

        /**
         * Generates a four-digit number for a user id (includes leading zeros).
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
            while (true)
            {
                val input = Packet.read(clientIn)

                if (input is Packet.Message)
                    server.forwardMessageFromUser(input, sender = this)
            }
        }
        catch (e: Exception)
        {
            server.sendMessage(L10n["user.left", name])
            server.mutableUsers -= this
        }
        finally
        {
            closeAll()
        }

    }

    override fun sendMessage(msg: Packet.Message)
    {
        try
        {
            msg.write(clientOut)
            clientOut.flush()
        }
        catch (e: SocketException)
        {
            closeAll()
        }
    }

    override fun kick() = closeAll()

    private fun closeAll()
    {
        client.close()
        clientIn.close()
        clientOut.close()
    }
}
