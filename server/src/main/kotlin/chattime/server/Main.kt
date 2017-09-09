/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

internal val properties = Properties()
private val propertiesPath = Paths.get("config.properties")

fun main(args: Array<String>)
{
    println("ChatTime server starting up!")

    val server = ChatServer()

	loadProperties()

	if (properties.getProperty("server.port") == null)
		properties.put("server.port", "25550")

	val port = properties.getProperty("server.port").toInt()
    val socket = ServerSocket(port)

    println("Socket opened at $port")

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

private fun serverToClients(server: ChatServer)
{
    do
    {
        val input = readLine()

        if (input != null && input != "") // "input.isNullOrEmpty()" didn't do the "smart cast" :-(
            server.receiveAndPushMessage(input, server)
    } while (input != null)
}

private fun loadProperties()
{
    // Initialize default values
	// No defaults right now

    // Prevents a NoSuchFileException
	if (Files.notExists(propertiesPath))
        Files.createFile(propertiesPath)

    properties.load(Files.newInputStream(propertiesPath))
}

internal fun saveProperties()
{
    properties.store(Files.newOutputStream(propertiesPath), "ChatTime server properties")
}
