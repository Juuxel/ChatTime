/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.net

import chattime.api.net.Packet.Companion
import com.beust.klaxon.*
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.reflect.full.isSuperclassOf

/**
 * A packet for client-server communications, identified with a unique [id].
 *
 * @property id the unique ID for the packet type
 */
sealed class Packet(val id: Int)
{
    /**
     * The companion object of `Packet`.
     *
     * Implements the `Converter` interface from Klaxon and provides functions for converting
     * packets to and from JSON.
     */
    companion object : Converter
    {
        /**
         * Returns `true` if [cls] is `Packet::class` or a superclass of `Packet`.
         */
        override fun canConvert(cls: Class<*>) = cls.kotlin.isSuperclassOf(Packet::class)

        /**
         * Converts the JSON value ([jv]) to a `Packet`.
         *
         * @throws IllegalArgumentException if [jv] does not contain a `JsonObject`
         * @throws NoSuchElementException if a packet property is missing
         */
        override fun fromJson(jv: JsonValue): Packet
        {
            if (jv.obj == null)
                throw IllegalArgumentException("jv must contain an object")

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

        /**
         * Converts the [value] to JSON by calling `toJson(Packet)`.
         *
         * @throws IllegalArgumentException if the [value] is not a `Packet`
         */
        override fun toJson(value: Any) = toJson(value as? Packet
                                                     ?: throw IllegalArgumentException("value is not a Packet"))

        /**
         * Converts the [packet] to JSON.
         */
        fun toJson(packet: Packet) = json {
            obj("id" to packet.id,
                *packet.toMap().entries.map(Map.Entry<String, *>::toPair).toTypedArray())
        }.toJsonString()

        /**
         * Reads a packet from the input [stream].
         *
         * @see fromJson
         * @see write
         */
        fun read(stream: DataInputStream): Packet
        {
            val string = stream.readUTF()
            return Klaxon()
                .converter(this)
                .parse(json = string)!!
        }
    }

    /**
     * A message.
     *
     * Packet ID: 0
     *
     * @property sender the message sender
     * @property message the message content
     */
    data class Message(val sender: String, val message: String) : Packet(0)
    {
        override fun toMap() = mapOf("sender" to sender, "message" to message)
    }

    /**
     * Converts this packet's properties to a `Map`.
     */
    internal abstract fun toMap() : Map<String, *>

    /**
     * Writes this packet to the [outputStream].
     *
     * @see toJson
     * @see read
     */
    fun write(outputStream: DataOutputStream)
        = outputStream.writeUTF(toJson(this))
}
