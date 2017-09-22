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
import chattime.server.api.plugin.LoadOrder
import chattime.server.api.plugin.Plugin

typealias Command = Pair<String, (CommandPlugin, MessageEvent) -> Unit>

class CommandPlugin : Plugin
{
    override val id = "Commands"
    override val loadOrder = emptyList<LoadOrder>()

    private val mCommands: ArrayList<Command> = arrayListOf(
            "help" to CommandPlugin::help,
            "id" to CommandPlugin::id,
            "rename" to CommandPlugin::rename,
            "plugins" to CommandPlugin::plugins,
            "silent" to CommandPlugin::runSilently
    )

    val commands: List<Command>
        get() = mCommands

    override fun load(event: ServerEvent)
    {
        event.server.pushMessage("The Commands plugin loaded.")
    }

    override fun onUserJoin(event: UserEvent)
    {
        val userList = listOf(event.user)

        event.server.pushMessage("Commands are enabled in this chat room.", whitelist = userList)
        event.server.pushMessage("Type !help for more information.", whitelist = userList)
    }

    override fun onMessageReceived(event: MessageEvent)
    {
        if (event.msg.startsWith("!") && event.msg != "!")
            processCommand(event)
    }

    override fun handlePluginMessage(event: PluginMessageEvent)
    {
        if (event.msg is AddCommandMessage)
        {
            event.server.pushMessage("[Commands] Adding commands from ${event.sender.id}", whitelist = listOf(event.server.serverUser))
            mCommands.add(event.msg.commandName to event.msg.function)
        }
    }

    private fun processCommand(event: MessageEvent)
    {
        val commandName = getCommandParams(event.msg)[0]
        var commandFound = false

        mCommands.forEach {
            if (it.first == commandName)
            {
                it.second(this, event)
                commandFound = true
            }
        }

        if (!commandFound)
            event.server.pushMessage("Command '$commandName' not found.", whitelist = listOf(event.sender))
    }

    // UTILS //

    private fun pluginMessage(event: MessageEvent, cmd: String, msg: String,
                              blacklist: Collection<User> = emptyList(),
                              whitelist: Collection<User> = emptyList())
    {
        event.server.pushMessage("[Commands] $cmd: $msg", blacklist, whitelist)
    }

    fun getCommandParams(msg: String): List<String>
    {
        val hasSpace = msg.indexOf(' ') != -1

        return if (hasSpace) msg.substring(1).split(' ')
               else listOf(msg.substring(1))
    }

    // COMMANDS //

    private fun help(event: MessageEvent)
    {
        val whitelist = listOf(event.sender)

        pluginMessage(event, "help", "List of commands available:", whitelist = whitelist)

        commands.sortedBy { it.first }.forEach {
            event.server.pushMessage("- ${it.first}", whitelist = whitelist)
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
            pluginMessage(event, "rename", "Type a new name.", whitelist = listOf(event.sender))
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
        val whitelist = listOf(event.sender)

        pluginMessage(event, "plugins", "List of loaded plugins:", whitelist = whitelist)

        event.server.plugins.sortedBy { it.id }.forEach {
            event.server.pushMessage("- ${it.id}", whitelist = whitelist)
        }
    }

    private fun runSilently(event: MessageEvent)
    {
        val params = getCommandParams(event.msg)

        if (params.size == 1)
        {
            pluginMessage(event, "silent", "Usage: !silent <command>", whitelist = listOf(event.sender))
            return
        }

        val newMsg = '!' + params.subList(1, params.size).joinToString(" ")

        processCommand(MessageEvent(event.server, newMsg, event.sender))
        event.cancel()
    }

    // PLUGIN MESSAGES //
    class AddCommandMessage(val commandName: String, val function: (CommandPlugin, MessageEvent) -> Unit)
    {
        constructor(commandName: String, function: (MessageEvent) -> Unit)
            : this(commandName, { _: CommandPlugin, event: MessageEvent -> function(event) })
    }
}
