package chattime.server.plugins

import chattime.server.event.*

interface Plugin
{
    val name: String

    fun onPluginLoad(event: ServerEvent)
    {}

    fun onUserJoin(event: UserEvent)
    {}

    fun onMessageReceived(event: MessageEvent)
    {}
}
