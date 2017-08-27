package chattime.server.plugins

import chattime.server.event.*

interface Plugin
{
    val name: String
    val id: String
    val loadOrder: List<LoadOrder>

    fun load(event: ServerEvent)
    {}

    fun onUserJoin(event: UserEvent)
    {}

    fun onMessageReceived(event: MessageEvent)
    {}

    fun handlePluginMessage(event: PluginMessageEvent)
    {}

    fun onPluginLoaded(event: PluginEvent)
    {}
}

sealed class LoadOrder(val id: String, val isRequired: Boolean)
{
    class Before(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
    class After(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
}
