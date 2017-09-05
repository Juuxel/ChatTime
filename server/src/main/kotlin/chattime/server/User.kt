package chattime.server

interface User
{
    val id: String
    var name: String
    var isEchoingEnabled: Boolean

    fun sendMessage(msg: String)
}
