/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.plugins

import chattime.api.event.EventType
import chattime.api.event.MessageEvent
import chattime.api.event.PluginLoadEvent
import chattime.api.event.UserJoinEvent
import chattime.api.features.Commands
import chattime.api.features.Commands.Command

class CommandPlugin : Commands
{
    override val id = "Commands"

    private val mutCommands: ArrayList<Command> = arrayListOf(
        construct("help", Desc.help, CommandPlugin::help),
        construct("id", Desc.id, CommandPlugin::id),
        construct("rename", Desc.rename, CommandPlugin::rename),
        construct("plugins", Desc.plugins, CommandPlugin::plugins),
        construct("silent", Desc.silent, CommandPlugin::runSilently),
        construct("users", Desc.users, CommandPlugin::users),
        construct("pm", Desc.pm, CommandPlugin::pm),
        construct("whois", Desc.whoIs, CommandPlugin::whoIs)
    )

    override val commands: List<Command>
        get() = mutCommands

    override fun load(event: PluginLoadEvent)
    {
        event.eventBus.subscribe(EventType.userJoin) { onUserJoin(it) }
        event.eventBus.subscribe(EventType.chatMessage) { onMessageReceived(it) }
    }

    private fun onUserJoin(event: UserJoinEvent)
    {
        val userList = listOf(event.user)

        event.server.sendMessage("Commands are enabled in this chat room.", whitelist = userList)
        event.server.sendMessage("Type !help for more information.", whitelist = userList)
    }

    private fun onMessageReceived(event: MessageEvent)
    {
        if (event.msg.startsWith("!") && event.msg != "!")
            processCommand(event)
    }

    private fun processCommand(event: MessageEvent)
    {
        val commandName = Commands.getCommandParams(event.msg)[0]
        var commandFound = false

        mutCommands.forEach {
            if (it.name == commandName)
            {
                it.handleMessage(event)
                commandFound = true
            }
        }

        if (!commandFound)
            event.server.sendMessage("Command '$commandName' not found.", whitelist = listOf(event.sender))
    }

    override fun addCommand(command: Commands.Command)
    {
        mutCommands += command
    }

    // UTILS //

    private fun pluginMessage(event: MessageEvent, cmd: String, msg: String)
    {
        event.sendMessageToSender("[Commands] $cmd: $msg")
    }

    private fun construct(name: String, desc: String, function: (CommandPlugin, MessageEvent) -> Unit)
        = Commands.construct(name, desc, { function(this, it) })

    // COMMANDS //

    private fun help(event: MessageEvent)
    {
        pluginMessage(event, "help", "List of commands available:")

        commands.sortedBy { it.name }.forEach {
            event.sendMessageToSender("- ${it.name}: ${it.description}")
        }
    }

    private fun id(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg,
                                               joinLastParam = true,
                                               lastParamIndex = 1)

        if (params.size > 1)
        {
            val userName = params[1]
            val users = event.server.users

            if (users.none { it.name.contains(userName, ignoreCase = true) })
                pluginMessage(event, "id", "No users found with name '$userName'")
            else
            {
                pluginMessage(event, "id",
                              "List of all users with '$userName' in their name:")

                users.filter {
                    it.name.contains(userName, ignoreCase = true)
                }.forEach {
                    event.sendMessageToSender("- ${it.name}: ${it.id}")
                }
            }
        }
        else
            pluginMessage(event, "id", event.sender.id)
    }

    private fun rename(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg,
                                               joinLastParam = true,
                                               lastParamIndex = 1)

        if (params.size == 1)
            pluginMessage(event, "rename", "Type a new name.")
        else
        {
            val oldName = event.sender.name
            val newName = params[1]
            event.sender.name = newName
            event.server.sendMessage("Renamed $oldName → $newName")
        }
    }

    private fun plugins(event: MessageEvent)
    {
        pluginMessage(event, "plugins", "List of loaded plugins:")

        event.server.plugins.sortedBy { it.id }.forEach {
            event.sendMessageToSender("- ${it.id}")
        }
    }

    private fun runSilently(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg,
                                               joinLastParam = true,
                                               lastParamIndex = 1)

        if (params.size == 1)
        {
            pluginMessage(event, "silent", "Usage: !silent <command>")
            return
        }

        processCommand(MessageEvent(event.server, "!${params[1]}", event.sender))
        event.cancel()
    }

    private fun users(event: MessageEvent)
    {
        pluginMessage(event, "users", "Users in the chat:")

        event.server.users.sortedBy { it.name }.forEach {
            event.sendMessageToSender("- ${it.name}")
        }
    }

    private fun pm(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg,
                                               joinLastParam = true,
                                               lastParamIndex = 2)

        if (params.size < 3)
        {
            pluginMessage(event, "pm", "Usage: !pm <user id> <msg>")
            return
        }

        val list = event.server.users.filter { it.id == params[1] }

        event.server.sendMessage("${event.sender.name} (PM): ${params[2]}", whitelist = list)
        event.cancel()
    }

    private fun whoIs(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg)

        if (params.size < 2)
        {
            pluginMessage(event, "whois", "Usage: !whois <user id>")
            return
        }

        val user = event.server.users.first { it.id == params[1] }.name

        event.sendMessageToSender("${params[1]} is $user")
    }

    object Desc
    {
        val attributes = "Manage user attributes"
        val help = "Shows information about commands"
        val id = "Shows user ids"
        val plugins = "Lists all plugins"
        val pm = "Message someone privately"
        val rename = "Rename yourself"
        val silent = "Debugs commands silently"
        val users = "Lists all users"
        val whoIs = "Shows the name of a user from an id"
    }
}
