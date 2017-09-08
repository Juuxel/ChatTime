/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

interface User
{
    val id: String
    var name: String
    var isEchoingEnabled: Boolean

    fun sendMessage(msg: String)
}
