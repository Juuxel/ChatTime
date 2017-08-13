package chattime.client

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

    serverOut.println("!setCliMode") // Send the command to the server to clean up output :-)

    Thread({ cliInput(serverOut) }).start()

    try
    {
        do
        {
            val output = serverIn.readLine()
            println(output)
        } while (output != null)
    }
    catch (e: Exception)
    {
        println("Disconnected from the server")
    }
}

fun cliInput(serverOut: PrintWriter)
{
    do
    {
        val input = readLine()

        if (input != "")
            serverOut.println(input)
    } while (input != null)
}
