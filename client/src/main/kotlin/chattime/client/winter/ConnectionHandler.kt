/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client.winter

import java.io.PrintWriter
import java.net.Socket
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class ConnectionHandler(private val socket: Socket)
{
    private val streamFromServer = socket.getInputStream()
    private val streamToServer = socket.getOutputStream()
    private val reader = streamFromServer.bufferedReader()
    private val printer = PrintWriter(streamToServer)

    private val msgHandlers: ArrayList<(String) -> Unit> = ArrayList()
    private val exitHandlers: ArrayList<() -> Unit> = ArrayList()

    init
    {
        thread {
            try
            {
                do
                {
                    val output = reader.readLine()
                    msgHandlers.forEach { it(output) }
                } while (output != null)
            }
            catch (e: Exception)
            {
                exitHandlers.forEach { it() }
            }
        }
    }

    fun handleMessage(block: (String) -> Unit)
    {
        msgHandlers += block
    }

    fun handleExit(block: () -> Unit)
    {
        exitHandlers += block
    }

    fun toServer(string: String)
    {
        printer.println(string)
        printer.flush()
    }
    fun close() = socket.close()
}
