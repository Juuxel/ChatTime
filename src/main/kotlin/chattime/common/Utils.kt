/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.common

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun formatTime(time: LocalTime): String = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
fun formatCurrentTime(): String = formatTime(LocalTime.now())

fun formatMessage(msg: String): String
        = "${formatCurrentTime()} | $msg"
