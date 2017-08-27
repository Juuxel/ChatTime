package chattime.server.plugins

import chattime.server.User
import chattime.server.event.*

typealias Command = Pair<String, (CommandPlugin, MessageEvent) -> Unit>

class CommandPlugin : Plugin
{
    override val name = "Commands"
    override val id = "Commands"
    override val loadOrder = emptyList<LoadOrder>()

    private val mCommands: ArrayList<Command> = arrayListOf(
            "help" to CommandPlugin::help,
            "id" to CommandPlugin::id,
            "rename" to CommandPlugin::rename,
            "toggleCliMode" to CommandPlugin::toggleCliMode, // TODO Maybe replace?
            "plugins" to CommandPlugin::plugins
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
        if (event.message is AddCommandMessage)
        {
            event.server.pushMessage("[Commands] Adding commands from ${event.sender.id}", whitelist = listOf(event.server))
            mCommands.add(event.message.commandName to event.message.function)
        }
    }

    private fun processCommand(event: MessageEvent)
    {
        // Get the command name
        val spaceIndex = event.msg.indexOf(' ')
        val commandName =
                if (spaceIndex == -1) event.msg.substring(1)
                else event.msg.substring(1, spaceIndex)
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

    private fun toggleCliMode(event: MessageEvent)
    {
        event.sender.isCliUser = !event.sender.isCliUser
    }

    private fun plugins(event: MessageEvent)
    {
        val whitelist = listOf(event.sender)

        pluginMessage(event, "plugins", "List of loaded plugins:", whitelist = whitelist)

        event.server.plugins.sortedBy { it.name }.forEach {
            event.server.pushMessage("- ${it.name} (${it.id})", whitelist = whitelist)
        }
    }

    // PLUGIN MESSAGES //
    class AddCommandMessage(val commandName: String, val function: (CommandPlugin, MessageEvent) -> Unit)
    {
        constructor(commandName: String, function: (MessageEvent) -> Unit)
            : this(commandName, { _: CommandPlugin, event: MessageEvent -> function(event) })
    }
}
