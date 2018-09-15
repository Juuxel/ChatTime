/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
@file:JvmName("CliClient")
package chattime.client

import chattime.common.formatMessage
import picocli.CommandLine
import java.net.ConnectException
import java.net.InetAddress

@CommandLine.Command(name = "cli", description = ["launch the cli client"])
internal class CliClient : Runnable
{
    @CommandLine.Parameters(index = "0", arity = "1", description = ["the server address"])
    lateinit var host: InetAddress

    @CommandLine.Parameters(index = "1", arity = "1", description = ["the server port"])
    var port: Int = 0

    override fun run()
    {
        println("ChatTime client starting up!")

        try
        {
            val connection = Connection(host, port)

            println("Connected to the server at $host:$port")

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
}

