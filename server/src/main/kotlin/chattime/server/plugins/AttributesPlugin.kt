/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.plugins

import chattime.server.api.User
import chattime.server.api.event.MessageEvent
import chattime.server.api.event.PluginMessageEvent
import chattime.server.api.event.ServerEvent
import chattime.server.api.event.UserEvent
import chattime.server.api.features.Commands
import chattime.server.api.features.Features
import chattime.server.api.plugin.LoadOrder
import chattime.server.api.plugin.Plugin

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
        event.server.getFeaturePlugin(Features.commands).addCommand(object : Commands.Command {
            override val commandName = "attributes"

            override fun handleMessage(event: MessageEvent)
            {
                attributeCommand(event)
            }
        })

        // Add the server user attributes
        userAttributes[event.server.serverUser] = HashMap()
        userAttributes[event.server.serverUser]!!["isEchoingEnabled"] = "true"
    }

    override fun onUserJoin(event: UserEvent)
    {
        userAttributes[event.user] = HashMap()

        // Add default data
        userAttributes[event.user]!!["isEchoingEnabled"] = event.user.isEchoingEnabled.toString()
    }

    private fun attributeCommand(event: MessageEvent)
    {
        val params = CommandPlugin.getCommandParams(event.msg)

        fun listSubCommands() {
            event.sendMessageToSender("[Attributes] List of subcommands:")
            listOf("get", "set", "list").forEach { event.sendMessageToSender("- $it") }
        }

        if (params.size < 2)
        {
            event.sendMessageToSender("[Attributes] Please provide a subcommand.")
            listSubCommands()
            return
        }

        when (params[1])
        {
            "get" -> {
                if (params.size < 3)
                    event.sendMessageToSender("[Attributes] Usage of 'get': !attributes get <id>")
                else
                {
                    val value = userAttributes[event.sender]!![params[2]]

                    if (value != null)
                        event.sendMessageToSender("[Attributes] Value of ${params[2]} is $value")
                    else
                        event.sendMessageToSender("[Attributes] Value of ${params[2]} has not been set")
                }
            }

            "set" -> {
                if (params.size < 4)
                    event.sendMessageToSender("[Attributes] Usage of 'set': !attributes set <id> <value>")
                else
                {
                    event.sendMessageToSender("[Attributes] Setting the value of ${params[2]} to ${params[3]}")
                    userAttributes[event.sender]!![params[2]] = params[3]

                    attributeHooks.filter { it.first == params[2] }.forEach {
                        it.second(event.sender, params[3])
                    }
                }
            }

            "list" -> {
                event.sendMessageToSender("[Attributes] Your attributes:")

                userAttributes[event.sender]!!.forEach { id: String, value: String ->
                    event.sendMessageToSender("$id: $value")
                }
            }

            else -> {
                event.sendMessageToSender("[Attributes] Unknown subcommand: ${params[1]}")
                listSubCommands()
            }
        }
    }

    override fun handlePluginMessage(event: PluginMessageEvent)
    {
        if (event.msg is AddAttributeHookMessage)
        {
            val msg = event.msg as AddAttributeHookMessage

            event.server.sendMessage("[Attributes] Adding a hook for ${msg.attributeId} from ${event.sender.id}",
                                     whitelist = listOf(event.server.serverUser))
            mHooks.add(msg.attributeId to msg.hook)
        }
    }

    class AddAttributeHookMessage(val attributeId: String, val hook: (User, String) -> Unit)
}
