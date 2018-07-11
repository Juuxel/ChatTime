/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
@file:JvmName("CliClient")
package chattime.client

import chattime.common.Info
import chattime.common.formatMessage
import picocli.CommandLine
import java.net.ConnectException
import java.net.InetAddress
import java.net.Socket

fun main(args: Array<String>)
{
    val params = CliParams()
    val commandLine = CommandLine(params)
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

    println("ChatTime client starting up!")

    try
    {
        val socket = Socket(params.host, params.port)
        val connection = Connection(socket)

        println("Connected to the server at ${params.host}:${params.port}")

        connection.toServer("!silent attributes set isEchoingEnabled false") // Send the command to the server to clean up output :-)

        Thread { cliInput(connection) }.start()

        connection.handleMessage {
            println(formatMessage(it))
        }

        connection.handleExit {
            println("Disconnected from the server")
            System.exit(0)
        }
    }
    catch (e: ConnectException)
    {
        System.err.println("Failed to connect to the server. Stack trace:")
        e.printStackTrace()
    }
}

private fun cliInput(connection: Connection)
{
    try
    {
        while (true)
        {
            val input = readLine() ?: continue

            if (input != "")
                connection.toServer(input)
        }
    }
    finally
    {
        connection.close()
    }
}

@CommandLine.Command(name = "chattime-client", version = [Info.fullVersion])
internal class CliParams
{
    @CommandLine.Parameters(index = "0", arity = "1", description = ["the server address"])
    var host: InetAddress? = null

    @CommandLine.Parameters(index = "1", arity = "1", description = ["the server port"])
    var port: Int = 0

    @Suppress("unused")
    @CommandLine.Option(names = ["-h", "--help"],
                        usageHelp = true, description = ["display usage info"])
    var isHelpRequested: Boolean = true

    @Suppress("unused")
    @CommandLine.Option(names = ["-V", "--version"],
                        versionHelp = true, description = ["display version info"])
    var isVersionRequested: Boolean = false
}
