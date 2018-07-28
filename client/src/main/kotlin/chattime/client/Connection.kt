/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client

import chattime.api.net.Packet
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class Connection(private val socket: Socket)
{
    private val streamFromServer = DataInputStream(socket.getInputStream())
    private val streamToServer = DataOutputStream(socket.getOutputStream())

    private val msgHandlers: ArrayList<(Packet.Message) -> Unit> = ArrayList()
    private val exitHandlers: ArrayList<() -> Unit> = ArrayList()

    init
    {
        thread {
            try
            {
                while (true)
                {
                    val packet = Packet.read(streamFromServer)

                    if (packet is Packet.Message)
                        msgHandlers.forEach { it(packet) }
                }
            }
            catch (e: Exception)
            {
                exitHandlers.forEach { it() }
            }
        }
    }

    fun handleMessage(block: (Packet.Message) -> Unit)
    {
        msgHandlers += block
    }

    fun handleExit(block: () -> Unit)
    {
        exitHandlers += block
    }

    fun toServer(string: String)
    {
        Packet.Message("", string).write(streamToServer)
        streamToServer.flush()
    }

    fun close() = socket.close()
}
