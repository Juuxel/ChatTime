package chattime.server.plugins

import chattime.server.User
import chattime.server.event.*

class CommandPlugin : Plugin
{
    override val name = "Commands"

    val commands = listOf(
            "help" to CommandPlugin::help,
            "id" to CommandPlugin::id,
            "rename" to CommandPlugin::rename,
            "setCliMode" to CommandPlugin::setCliMode
    )

    override fun onPluginLoad(event: ServerEvent)
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

    private fun processCommand(event: MessageEvent)
    {
        // Get the command name
        val spaceIndex = event.msg.indexOf(' ')
        val commandName =
                if (spaceIndex == -1) event.msg.substring(1)
                else event.msg.substring(1, spaceIndex)

        commands.forEach {
            if (it.first == commandName)
                it.second(this, event)
        }
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

        commands.forEach {
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

    private fun setCliMode(event: MessageEvent)
    {
        event.sender.isCliUser = true
    }
}