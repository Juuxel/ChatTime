package chattime.server.plugins

import chattime.server.User
import chattime.server.event.MessageEvent
import chattime.server.event.PluginMessageEvent
import chattime.server.event.ServerEvent
import chattime.server.event.UserEvent

class AttributesPlugin : Plugin
{
    override val id: String = "Attributes"
    override val loadOrder = listOf(LoadOrder.After("Commands", isRequired = true))
    val attributeHooks: List<Pair<String, (User, String) -> Unit>>
        get() = mHooks

    private val mHooks: ArrayList<Pair<String, (User, String) -> Unit>>
        = arrayListOf(
            "isEchoingEnabled" to { user: User, s: String ->
                user.isEchoingEnabled = s.toBoolean()
            }
        )

    private val userAttributes: HashMap<User, HashMap<String, String>> = HashMap()

    override fun load(event: ServerEvent)
    {
        val commandFunction =  { plugin: CommandPlugin, msgEvent: MessageEvent ->
            attributeCommand(plugin, msgEvent)
        }

        event.server.sendPluginMessage("Commands", this,
                                       CommandPlugin.AddCommandMessage("attributes", commandFunction))

        // Add the server user attributes
        userAttributes[event.server] = HashMap()
        userAttributes[event.server]!!["isEchoingEnabled"] = "true"
    }

    override fun onUserJoin(event: UserEvent)
    {
        userAttributes[event.user] = HashMap()

        // Add default data
        userAttributes[event.user]!!["isEchoingEnabled"] = event.user.isEchoingEnabled.toString()
    }

    private fun attributeCommand(plugin: CommandPlugin, event: MessageEvent)
    {
        val params = plugin.getCommandParams(event.msg)

        fun listSubCommands() {
            event.pushMessageToSender("[Attributes] List of subcommands:")
            listOf("get", "set", "list").forEach { event.pushMessageToSender("- $it") }
        }

        if (params.size < 2)
        {
            event.pushMessageToSender("[Attributes] Please provide a subcommand.")
            listSubCommands()
            return
        }

        when (params[1])
        {
            "get" -> {
                if (params.size < 3)
                    event.pushMessageToSender("[Attributes] Usage of 'get': !attributes get <id>")
                else
                {
                    val value = userAttributes[event.sender]!![params[2]]

                    if (value != null)
                        event.pushMessageToSender("[Attributes] Value of ${params[2]} is $value")
                    else
                        event.pushMessageToSender("[Attributes] Value of ${params[2]} has not been set")
                }
            }

            "set" -> {
                if (params.size < 4)
                    event.pushMessageToSender("[Attributes] Usage of 'set': !attributes set <id> <value>")
                else
                {
                    event.pushMessageToSender("[Attributes] Setting the value of ${params[2]} to ${params[3]}")
                    userAttributes[event.sender]!![params[2]] = params[3]

                    attributeHooks.filter { it.first == params[2] }.forEach {
                        it.second(event.sender, params[3])
                    }
                }
            }

            "list" -> {
                event.pushMessageToSender("[Attributes] Your attributes:")

                userAttributes[event.sender]!!.forEach { id: String, value: String ->
                    event.pushMessageToSender("$id: $value")
                }
            }

            else -> {
                event.pushMessageToSender("[Attributes] Unknown subcommand: ${params[1]}")
                listSubCommands()
            }
        }
    }

    override fun handlePluginMessage(event: PluginMessageEvent)
    {
        if (event.msg is AddAttributeHookMessage)
        {
            event.server.pushMessage("[Attributes] Adding a hook for ${event.msg.attributeId} from ${event.sender.id}",
                                     whitelist = listOf(event.server))
            mHooks.add(event.msg.attributeId to event.msg.hook)
        }
    }

    class AddAttributeHookMessage(val attributeId: String, val hook: (User, String) -> Unit)
}
