/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server.plugins

import chattime.api.event.*
import chattime.api.features.Commands
import chattime.api.features.Commands.Command
import chattime.api.net.Packet
import chattime.common.Info
import chattime.server.L10n

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
        construct("who-is", Desc.whoIs, CommandPlugin::whoIs),
        construct("about", Desc.ctInfo, CommandPlugin::ctInfo),
        construct("kick", Desc.kick, CommandPlugin::kick)
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

        event.server.sendMessage("%s%n%s".format(L10n["commands.start1"], L10n["commands.start2"]), whitelist = userList)
    }

    private fun onMessageReceived(event: MessageEvent)
    {
        if (event.msg.message.startsWith("!") && event.msg.message != "!")
            processCommand(event)
    }

    private fun processCommand(event: MessageEvent)
    {
        val commandName = Commands.getCommandParams(event.msg.message)[0]
        var commandFound = false
        val commandEvent = CommandEvent(event.server, commandName,
                                        event.msg, event.sender)

        // Fire the command event
        event.eventBus.post(commandEvent)

        // Check if canceled
        if (commandEvent.isCanceled)
            return

        // Handle the command
        mutCommands.forEach {
            if (it.name == commandName)
            {
                it.handleMessage(event)
                commandFound = true
            }
        }

        if (!commandFound)
            event.server.sendMessage(L10n["commands.commandNotFound", commandName],
                                     whitelist = listOf(event.sender))
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
        = Commands.construct(name, desc) { function(this, it) }

    // COMMANDS //

    private fun help(event: MessageEvent)
    {
        pluginMessage(event, "help", L10n["commands.help.all"])

        commands.sortedBy { it.name }
            .joinToString(separator = "\n") { "- ${it.name}: ${it.description}" }
            .let(event::sendMessageToSender)
    }

    private fun id(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg.message,
                                               joinLastParam = true,
                                               lastParamIndex = 1)

        if (params.size > 1)
        {
            val userName = params[1]
            val users = event.server.users

            if (users.none { it.name.contains(userName, ignoreCase = true) })
                pluginMessage(event, "id", L10n["commands.id.noUsersFound", userName])
            else
            {
                pluginMessage(event, "id", L10n["commands.id.usersFound", userName])

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
        val params = Commands.getCommandParams(event.msg.message,
                                               joinLastParam = true,
                                               lastParamIndex = 1)

        if (params.size == 1)
            pluginMessage(event, "rename", L10n["commands.rename.noNameGiven"])
        else
        {
            val oldName = event.sender.name
            val newName = params[1]
            event.sender.name = newName
            event.server.sendMessage(L10n["commands.rename.renamed"].format(oldName, newName))
        }
    }

    private fun plugins(event: MessageEvent)
    {
        pluginMessage(event, "plugins", L10n["commands.plugins.all"])

        event.server.plugins.sortedBy { it.id }.forEach {
            event.sendMessageToSender("- ${it.id}")
        }
    }

    private fun runSilently(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg.message,
                                               joinLastParam = true,
                                               lastParamIndex = 1)

        if (params.size == 1)
        {
            pluginMessage(event, "silent", "Usage: !silent <command>")
            return
        }

        processCommand(MessageEvent(event.server, Packet.Message(event.sender.id, "!${params[1]}"), event.sender))
        event.cancel()
    }

    private fun users(event: MessageEvent)
    {
        pluginMessage(event, "users", L10n["commands.users.all"])

        event.server.users.sortedBy { it.name }.forEach {
            event.sendMessageToSender("- ${it.name}")
        }
    }

    private fun pm(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg.message,
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
        val params = Commands.getCommandParams(event.msg.message)

        if (params.size < 2)
        {
            pluginMessage(event, "who-is", "Usage: !who-is <user id>")
            return
        }

        try
        {
            val user = event.server.getUserById(params[1]).name

            event.sendMessageToSender(L10n["commands.who-is.message", params[1], user])
        }
        catch (iae: IllegalArgumentException)
        {
            event.sendMessageToSender(iae.message ?: L10n["error.generic"])
        }
    }

    private fun ctInfo(event: MessageEvent)
    {
        event.sendMessageToSender("""
                                  ${Info.fullVersion}
                                  Made by Juuxel
                                  Licensed under MPL v2.0
                                  See more info at ${Info.url}
                                  -=-=-=-=-
                                  Open Source Libraries:
                                  - picocli (https://github.com/remkop/picocli)
                                  - RxJava (https://github.com/ReactiveX/RxJava)
                                  - RxKotlin (https://github.com/ReactiveX/RxKotlin)
                                  """.trimIndent())
    }

    private fun kick(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg.message, joinLastParam = true,
                                               lastParamIndex = 2)

        if (params.size < 3)
        {
            pluginMessage(event, "kick", "Usage: !kick <user id> <msg>")
            return
        }

        try
        {
            event.server.getUserById(params[1]).kick()
            event.server.sendMessage(L10n["commands.kick.message", event.sender.name, params[2]],
                                     whitelist = listOf(event.server.getUserById(params[1])))

            pluginMessage(event, "kick", L10n["commands.kick.kickerMessage", params[1]])
        }
        catch (iae: IllegalArgumentException)
        {
            event.sendMessageToSender(iae.message ?: L10n["error.generic"])
        }
    }

    object Desc // TODO Translate command desc
    {
        val attributes = "Manage user attributes"
        val ctInfo = L10n["commands.about.desc"]
        val help = "Shows information about commands"
        val id = "Shows user ids"
        val plugins = "Lists all plugins"
        val pm = L10n["commands.pm.desc"]
        val rename = "Rename yourself"
        val silent = "Debugs commands silently"
        val users = "Lists all users"
        val whoIs = "Shows the name of a user from an id"
        val kick = "Kick users from the chat"
    }
}
