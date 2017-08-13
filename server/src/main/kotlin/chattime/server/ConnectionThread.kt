package chattime.server

import java.io.PrintWriter
import java.net.Socket
import java.util.*

class ConnectionThread(val client: Socket, val server: ChatServer) : Runnable, User
{
    override val id = UUID.randomUUID().toString() //TODO Temporary ID
    override var name = id
    override var isCliUser = false
    val clientIn = Scanner(client.inputStream)
    val clientOut = PrintWriter(client.outputStream, true)

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
        }

    }

    override fun sendMessage(msg: String)
    {
        clientOut.println(msg)
    }
}
