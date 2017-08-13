package chattime.server

import chattime.server.plugins.CommandPlugin
import java.net.ServerSocket

fun main(args: Array<String>)
{
    if (args.isEmpty())
        println("Usage: chattime-server <port>")

    println("ChatTime server starting up!")

    val server = ChatServer()
    val socket = ServerSocket(args[0].toInt())

    println("Socket opened at ${args[0].toInt()}")

    Thread({ serverToClients(server) }).start()
    initPlugins(server)

    do
    {
        val client = socket.accept()
        val thread = ConnectionThread(client, server)

        Thread(thread).start()
        server.addThread(thread)
    } while (client != null)
}

//TODO Proper plugin init
private fun initPlugins(server: ChatServer)
{
    server.addPlugin(CommandPlugin())
}

fun serverToClients(server: ChatServer)
{
    do
    {
        val input = readLine()

        if (input != null && input != "") // "input.isNullOrEmpty()" didn't do the "smart cast" :-(
            server.receiveAndPushMessage(input, server)
    } while (input != null)
}
