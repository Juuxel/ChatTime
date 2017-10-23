/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client

import chattime.common.Info
import chattime.common.formatMessage
import picocli.CommandLine
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket

fun cliStart(args: Array<String>)
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

    val server = Socket(params.host, params.port)

    println("Connected to the server at ${params.host}:${params.port}")

    val serverIn = server.inputStream.bufferedReader()
    val serverOut = PrintWriter(server.outputStream, true)

    serverOut.println("!silent attributes set isEchoingEnabled false") // Send the command to the server to clean up output :-)

    Thread({ cliInput(serverOut) }).start()

    try
    {
        do
        {
            val output = serverIn.readLine()
            println(formatMessage(output))
        } while (output != null)
    }
    catch (e: Exception)
    {
        println("Disconnected from the server")
        System.exit(0)
    }
}

private fun cliInput(serverOut: PrintWriter)
{
    do
    {
        val input = readLine()

        if (input != "")
            serverOut.println(input)
    } while (input != null)
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
