/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

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
    server.pluginLoader.findPluginsFromDirectory("plugins")
    server.pluginLoader.loadPlugins()

    do
    {
        val client = socket.accept()
        val thread = ConnectionThread(client, server)

        Thread(thread).start()
        server.addThread(thread)
    } while (client != null)
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
