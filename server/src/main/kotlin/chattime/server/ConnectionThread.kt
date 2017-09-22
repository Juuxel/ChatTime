/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

import chattime.server.api.User
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class ConnectionThread(private val client: Socket, private val server: ChatServer) : Runnable, User
{
    override val id = UUID.randomUUID().toString()
    override var name = id
    override var isEchoingEnabled = true
    private val clientIn = Scanner(client.inputStream, Charsets.UTF_8.name())
    private val clientOut = PrintWriter(client.outputStream.bufferedWriter(Charsets.UTF_8), true)

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
            server.pushMessage("$name left the chat room")
            client.close() // Close the socket just in case
        }

    }

    override fun sendMessage(msg: String)
    {
        clientOut.println(msg)
    }
}
