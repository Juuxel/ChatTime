package chattime.server.plugins

import chattime.server.event.MessageEvent
import chattime.server.event.ServerEvent

class AttributesPlugin : Plugin
{
    override val name: String = "User Attributes"
    override val id: String = "Attributes"
    override val loadOrder = listOf(LoadOrder.After("Commands", isRequired = true))

    override fun load(event: ServerEvent)
    {
        val commandFunction =  { msgEvent: MessageEvent ->
            attributeCommand(msgEvent)
        }

        event.server.sendPluginMessage("Commands", this,
                                       CommandPlugin.AddCommandMessage("attribute", commandFunction))
    }

    private fun attributeCommand(event: MessageEvent)
    {
        event.msg = "I edited this message >:-)"
    }
}
