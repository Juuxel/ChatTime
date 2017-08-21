package chattime.server

import java.io.PrintWriter
import java.net.Socket
import java.util.*

class ConnectionThread(private val client: Socket, private val server: ChatServer) : Runnable, User
{
    override val id = UUID.randomUUID().toString()
    override var name = id
    override var isCliUser = false
    private val clientIn = Scanner(client.inputStream)
    private val clientOut = PrintWriter(client.outputStream, true)

    override fun run()
    {
        try
        {
            do
            {
                val input = clientIn.nextLine()
                server.receiveAndPushMessage(input, this)
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
