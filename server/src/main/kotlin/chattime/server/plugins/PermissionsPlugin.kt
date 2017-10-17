/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server.plugins

import chattime.api.User
import chattime.api.event.*
import chattime.api.features.Commands
import chattime.api.features.Permissions
import chattime.api.features.Permissions.*
import chattime.server.Strings

class PermissionsPlugin : Permissions
{
    private val permissions: HashMap<User, ArrayList<Permission>>
    = HashMap()

    private val globals: HashMap<String, Boolean>
    = HashMap()

    private val addPermission = "command.permissions.add"
    private val resetPermission = "command.permissions.reset"
    private val listPermission = "command.permissions.list"

    override val id = "Permissions"

    override fun load(event: PluginLoadEvent)
    {
        event.eventBus.subscribe(EventType.commandCall) { handleCommand(it) }
        event.eventBus.subscribe(EventType.userJoin) { handleUserJoin(it) }

        event.server.commandsPlugin.addCommand(
            Commands.construct("permissions", "Handle permissions.") {
                permissionCommand(it)
            }
        )

        /// Default permissions ///

        // Forbid the use of permissions add and reset by default
        globals[addPermission] = false
        globals[resetPermission] = false

        // Forbid kick by default
        globals["commands.kick"] = false
    }

    override fun addGlobalPermission(permission: Permission)
    {
        globals[permission.action] = permission.isAllowed
    }

    override fun hasPermission(user: User, action: String): Boolean
        = globals[action] == true
          || permissions[user]?.any {
              it.action == action && it.isAllowed
          } == true
          || user.id == "Server"

    private fun handleUserJoin(event: UserJoinEvent)
    {
        permissions[event.user] = ArrayList()
    }

    private fun handleCommand(event: CommandEvent)
    {
        val userPerms = permissions[event.sender] ?: return

        fun anyPerm(isAllowed: Boolean): Boolean
            = userPerms.any {
            it.action == event.commandName
            && it.isAllowed == isAllowed
        }

        if (
        (globals["command." + event.commandName] == false
            && !anyPerm(true)) // No allows for forbidden cmd
            || anyPerm(false)) // Forbids for regular cmd
        {
            event.forbidMessage('!' + event.commandName)
            event.cancel()
        }
    }

    private fun permissionCommand(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg)

        if (params.size < 2)
        {
            event.pluginMessage("Usage: !permissions <subcommand: list, add, reset>")
            return
        }

        when (params[1])
        {
            "list" -> {
                if (!hasPermission(event.sender, listPermission))
                {
                    event.forbidMessage(listPermission)
                    return
                }

                try
                {
                    val user = params.getOrElse(2) { event.sender.id }

                    event.pluginMessage("Permissions of $user:")

                    permissions[event.server.getUserById(user)]!!.forEach {
                        event.sendMessageToSender("- ${it.action}: ${it.isAllowed}")
                    }
                }
                catch (iae: IllegalArgumentException)
                {
                    event.sendMessageToSender(iae.message ?: Strings.unspecifiedError)
                }
            }

            "add" -> {
                if (!hasPermission(event.sender, addPermission))
                {
                    event.forbidMessage(addPermission)
                    return
                }

                if (params.size < 5)
                {
                    event.pluginMessage("Usage: !permissions add <user> <action> <isAllowed: true, false>")
                    return
                }

                try
                {
                    val user = params[2]
                    val action = params[3]
                    val isAllowed = params[4].toBoolean()
                    val userPermissions = permissions[event.server.getUserById(user)]!!

                    if (!hasPermission(event.sender, action))
                    {
                        event.pluginMessage("You can't give others permissions you don't have yourself.")
                        return
                    }

                    if (userPermissions.any { it.action == action })
                        userPermissions.removeAll { it.action == action }

                    userPermissions +=
                        Permission(action, isAllowed)

                    event.sendMessageToSender("Added a permission to $user for $action (allowed: $isAllowed)")
                }
                catch (iae: IllegalArgumentException)
                {
                    event.sendMessageToSender(iae.message ?: Strings.unspecifiedError)
                }
            }

            "reset" -> {
                if (!hasPermission(event.sender, resetPermission))
                {
                    event.forbidMessage(resetPermission)
                    return
                }

                if (params.size < 3)
                {
                    event.pluginMessage("Usage: !permissions reset <user> [<action>]")
                    return
                }

                try
                {
                    val user = params[2]
                    val command = params.getOrElse(3, { "" })

                    permissions[event.server.getUserById(user)]!!.removeAll {
                        command == "" || it.action == command
                    }

                    event.pluginMessage("Reset permissions of $user.")
                }
                catch (iae: IllegalArgumentException)
                {
                    event.sendMessageToSender(iae.message ?: Strings.unspecifiedError)
                }
            }

            else -> {
                event.pluginMessage("Unknown subcommand '${params[1]}'.")
            }
        }
    }

    private fun BaseMessageEvent.pluginMessage(msg: String)
    {
        sendMessageToSender("[Permissions] $msg")
    }

    private fun BaseMessageEvent.forbidMessage(action: String)
    {
        pluginMessage("Usage of $action denied.")
    }
}
