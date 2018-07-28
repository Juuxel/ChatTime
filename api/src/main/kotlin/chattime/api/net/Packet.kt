/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.net

import com.beust.klaxon.*
import java.io.DataInputStream
import kotlin.reflect.full.isSuperclassOf

sealed class Packet(val id: Int)
{
    companion object : Converter
    {
        override fun canConvert(cls: Class<*>) = cls.kotlin.isSuperclassOf(Packet::class)

        override fun fromJson(jv: JsonValue): Packet
        {
            if (jv.obj == null)
                throw KlaxonException("jv must contain an object")

            val obj = jv.obj!!
            val id = obj.int("id")!!

            return when (id)
            {
                0 -> {
                    val userId = obj.string("sender") ?: throw NoSuchElementException("Missing sender")
                    val message = obj.string("message") ?: throw NoSuchElementException("Missing message")
                    Message(userId, message)
                }

                else -> TODO("Handle other types")
            }
        }

        override fun toJson(value: Any) = toJson(value as Packet)

        fun toJson(packet: Packet) = json {
            obj("id" to packet.id,
                *packet.toMap().entries.map(Map.Entry<String, *>::toPair).toTypedArray())
        }.toJsonString()

        fun decode(stream: DataInputStream): Packet
        {
            val string = stream.readUTF()
            return Klaxon()
                .converter(this)
                .parse(json = string)!!
        }
    }

    data class Message(val sender: String, val message: String) : Packet(0)
    {
        override fun toMap() = mapOf("sender" to sender, "message" to message)
    }

    internal abstract fun toMap() : Map<String, *>

    @Suppress("NOTHING_TO_INLINE")
    inline fun toJson() = toJson(this)
}
