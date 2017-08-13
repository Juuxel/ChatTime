package chattime.server

interface User
{
    val id: String
    var name: String
    var isCliUser: Boolean

    fun sendMessage(msg: String)
}
