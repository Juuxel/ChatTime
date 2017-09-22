/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.plugins

import chattime.server.api.event.MessageEvent
import chattime.server.api.event.ServerEvent
import chattime.server.api.event.UserEvent
import chattime.server.api.features.Commands
import chattime.server.api.features.Commands.Command

class CommandPlugin : Commands
{
    override val id = "Commands"

    private val mutCommands: ArrayList<Command> = arrayListOf(
            FunctionCommand("help", CommandPlugin::help),
            FunctionCommand("id", CommandPlugin::id),
            FunctionCommand("rename", CommandPlugin::rename),
            FunctionCommand("plugins", CommandPlugin::plugins),
            FunctionCommand("silent", CommandPlugin::runSilently),
            FunctionCommand("users", CommandPlugin::users)
    )

    val commands: List<Command>
        get() = mutCommands

    companion object
    {
        fun getCommandParams(msg: String): List<String>
        {
            val hasSpace = msg.indexOf(' ') != -1

            return if (hasSpace) msg.substring(1).split(' ')
            else listOf(msg.substring(1))
        }
    }

    override fun load(event: ServerEvent)
    {
        event.server.sendMessage("The Commands plugin loaded.")
    }

    override fun onUserJoin(event: UserEvent)
    {
        val userList = listOf(event.user)

        event.server.sendMessage("Commands are enabled in this chat room.", whitelist = userList)
        event.server.sendMessage("Type !help for more information.", whitelist = userList)
    }

    override fun onMessageReceived(event: MessageEvent)
    {
        if (event.msg.startsWith("!") && event.msg != "!")
            processCommand(event)
    }

    private fun processCommand(event: MessageEvent)
    {
        val commandName = getCommandParams(event.msg)[0]
        var commandFound = false

        mutCommands.forEach {
            if (it.commandName == commandName)
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

    // COMMANDS //

    private fun help(event: MessageEvent)
    {
        pluginMessage(event, "help", "List of commands available:")

        commands.sortedBy { it.commandName }.forEach {
            event.server.sendMessage("- ${it.commandName}", whitelist = listOf(event.sender))
        }
    }

    private fun id(event: MessageEvent)
    {
        pluginMessage(event, "id", event.sender.id)
    }

    private fun rename(event: MessageEvent)
    {
        val spaceIndex = event.msg.indexOf(' ')

        if (spaceIndex == -1)
            pluginMessage(event, "rename", "Type a new name.")
        else
        {
            val oldName = event.sender.name
            val newName = event.msg.substring(spaceIndex + 1)
            event.sender.name = newName
            pluginMessage(event, "rename", "$oldName → $newName")
        }
    }

    private fun plugins(event: MessageEvent)
    {
        pluginMessage(event, "plugins", "List of loaded plugins:")

        event.server.plugins.sortedBy { it.id }.forEach {
            event.server.sendMessage("- ${it.id}", whitelist = listOf(event.sender))
        }
    }

    private fun runSilently(event: MessageEvent)
    {
        val params = getCommandParams(event.msg)

        if (params.size == 1)
        {
            pluginMessage(event, "silent", "Usage: !silent <command>")
            return
        }

        val newMsg = '!' + params.subList(1, params.size).joinToString(" ")

        processCommand(MessageEvent(event.server, newMsg, event.sender))
        event.cancel()
    }

    private fun users(event: MessageEvent)
    {
        pluginMessage(event, "users", "Users in the chat:")

        event.server.users.sortedBy { it.name }.forEach {
            event.server.sendMessage("- ${it.name}", whitelist = listOf(event.sender))
        }
    }

    private inner class FunctionCommand(override val commandName: String,
                                  val function: (CommandPlugin, MessageEvent) -> Unit) : Command
    {
        override fun handleMessage(event: MessageEvent)
        {
            function(this@CommandPlugin, event)
        }
    }
}
