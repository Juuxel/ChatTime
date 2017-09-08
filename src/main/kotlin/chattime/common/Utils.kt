/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.common

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun formatTime(time: LocalTime): String = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
fun formatCurrentTime(): String = formatTime(LocalTime.now())

fun formatMessage(msg: String): String
        = "${formatCurrentTime()} | $msg"
