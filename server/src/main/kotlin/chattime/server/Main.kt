/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server

import chattime.api.net.Packet
import chattime.common.Info
import picocli.CommandLine
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

internal val properties = Properties()
private val propertiesPath = Paths.get("config.properties")

fun main(args: Array<String>)
{
    val params = CliParams()
    val commandLine = CommandLine(params)

    commandLine.isUnmatchedArgumentsAllowed = true
    commandLine.parse(*args)

    if (commandLine.isUsageHelpRequested)
    {
        CommandLine.usage(params, System.out)
        return
    }
    else if (commandLine.isVersionHelpRequested)
    {
        commandLine.printVersionHelp(System.out)
        return
    }

    println(L10n["server.start"])

    val server = ChatServer()

	loadProperties()

	if (properties.getProperty("server.port") == null)
        properties["server.port"] = "25550"

	val port = properties.getProperty("server.port").toInt()
    val socket = ServerSocket(port)

    println(L10n["server.socketOpened", port])

    params.plugins.forEach {
        server.pluginLoader.addPlugin(className = it)
    }

    Thread { serverToClients(server) }.start()
    server.pluginLoader.findPluginsFromDirectory("plugins")
    server.pluginLoader.loadPlugins()

    do
    {
        val client = socket.accept()
        val thread = ConnectionThread(client, server)

        Thread(thread, thread.id).start()
        server.addUser(thread)
    } while (client != null)
}

private fun serverToClients(server: ChatServer)
{
    do
    {
        val input = readLine()

        if (input != null && input != "") // "!input.isNullOrEmpty()" didn't do the smart cast :-(
            server.forwardMessageFromUser(Packet.Message(server.serverUser.id, input), server.serverUser)
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

@CommandLine.Command(name = "chattime-server", version = [Info.fullVersion])
internal class CliParams
{
    @Suppress("unused")
    @CommandLine.Option(names = ["-h", "--help"],
                        usageHelp = true, description = ["display usage info"])
    var isHelpRequested: Boolean = false

    @Suppress("unused")
    @CommandLine.Option(names = ["-V", "--version"],
                        versionHelp = true, description = ["display version info"])
    var isVersionRequested: Boolean = false

    @CommandLine.Option(names = ["--plugins"],
                        description = ["add plugins from the classpath manually"])
    var plugins: List<String> = arrayListOf()
}
