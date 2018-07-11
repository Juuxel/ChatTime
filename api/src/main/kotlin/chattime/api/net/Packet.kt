/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.net

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer

sealed class Packet(val id: Byte, private vararg val content: Pair<String, StringLength>)
{
    companion object
    {
        fun decode(bytes: ByteArray): Packet
        {
            return ByteArrayInputStream(bytes).use(::decode)
        }

        fun decode(stream: InputStream): Packet
        {
            val id = stream.read()

            return when (id) {
                0 -> {
                    val userIdLength = stream.read()
                    val userId = ByteArray(userIdLength)
                    stream.read(userId)

                    val messageLength = stream.read()
                    val message = ByteArray(messageLength)
                    stream.read(message)

                    Message(String(userId, Charsets.UTF_8), String(message, Charsets.UTF_8))
                }

                else -> TODO("Handle other types")
            }
        }
    }

    constructor(id: Byte, vararg content: String) : this(id, *content.map { it to StringLength.BYTE }.toTypedArray())

    class Message(val sender: String, val message: String) : Packet(0, sender, message)

    /**
     * The required bytes to encode the length of a string.
     *
     * @property bytes the bytes
     */
    private enum class StringLength(val bytes: Int)
    {
        BYTE(1), SHORT(2)
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    fun encode(): ByteArray
    {
        val contentBytes = content.map { (string, length) -> (string as java.lang.String).getBytes(Charsets.UTF_8) to length }

        var size = 1

        for ((content, stringLength) in contentBytes)
            size += content.size + stringLength.bytes

        val buffer = ByteBuffer.allocate(size)
        buffer.put(id)

        for ((content, stringLength) in contentBytes)
        {
            when (stringLength)
            {
                StringLength.BYTE -> buffer.put(content.size.toByte())
                StringLength.SHORT -> buffer.putShort(content.size.toShort())
            }

            content.forEach { buffer.put(it) }
        }

        return buffer.array()
    }
}
