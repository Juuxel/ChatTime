/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server.plugins

import chattime.api.User
import chattime.api.event.*
import chattime.api.features.Commands
import chattime.api.plugin.LoadOrder
import chattime.api.plugin.Plugin

// TODO l10n
class AttributesPlugin : Plugin
{
    override val id: String = "Attributes"
    override val loadOrder = listOf(LoadOrder.After("Commands", isRequired = true))

    private val attributeHooks: ArrayList<Pair<String, (User, String) -> Unit>>
        = arrayListOf(
            "isEchoingEnabled" to { user: User, s: String ->
                user.isEchoingEnabled = s.toBoolean()
            }
        )

    private val userAttributes: HashMap<User, HashMap<String, String>> = HashMap()

    override fun load(event: PluginLoadEvent)
    {
        event.server.commandsPlugin.addCommand(
            Commands.construct("attributes", CommandPlugin.Desc.attributes) {
                attributeCommand(it)
            })

        event.eventBus.subscribe(EventType.userJoin, { onUserJoin(it) })
        event.eventBus.subscribe(EventType.pluginMessage, { handlePluginMessage(it) })

        // Add the server user attributes
        userAttributes[event.server.serverUser] = HashMap()
        userAttributes[event.server.serverUser]!!["isEchoingEnabled"] = "true"
    }

    private fun onUserJoin(event: UserJoinEvent)
    {
        userAttributes[event.user] = HashMap()

        // Add default data
        userAttributes[event.user]!!["isEchoingEnabled"] = event.user.isEchoingEnabled.toString()
    }

    private fun attributeCommand(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg.message)

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

    private fun handlePluginMessage(event: PluginMessageEvent)
    {
        if (event.msg is AddAttributeHookMessage)
        {
            val msg = event.msg as AddAttributeHookMessage

            event.server.sendMessage("[Attributes] Adding a hook for ${msg.attributeId} from ${event.sender.id}",
                                     whitelist = listOf(event.server.serverUser))
            attributeHooks += msg.attributeId to msg.hook
        }
    }

    class AddAttributeHookMessage(val attributeId: String, val hook: (User, String) -> Unit)
}
