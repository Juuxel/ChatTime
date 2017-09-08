/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.client

import chattime.common.formatMessage
import java.io.PrintWriter
import java.net.Socket

fun cliStart(args: Array<String>)
{
    if (args.size < 2)
    {
        println("Usage: chattime-client <host> <port>")
        System.exit(0)
    }

    println("ChatTime client starting up!")

    val server = Socket(args[0], args[1].toInt())

    println("Connected to the server at ${args[0]}:${args[1].toInt()}")

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
