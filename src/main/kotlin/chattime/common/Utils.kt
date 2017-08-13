package chattime.common

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun formatCurrentTime(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

fun formatMessage(msg: String): String
        = "${formatCurrentTime()} | $msg"

fun formatMessageWithSender(senderName: String, msg: String): String
        = formatMessage("/$senderName/ $msg")
