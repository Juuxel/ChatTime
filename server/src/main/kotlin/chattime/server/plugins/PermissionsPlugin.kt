/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server.plugins

import chattime.api.User
import chattime.api.event.*
import chattime.api.features.Commands
import chattime.api.features.Permissions
import chattime.api.features.Permissions.*

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

        // Forbid the use of permissions add and reset by default
        globals[addPermission] = false
        globals[resetPermission] = false

        // Add the server user's permission list
        permissions[event.server.serverUser] = ArrayList()

        // Allow the use of permissions to the server
        permissions[event.server.serverUser]!! +=
            Permission(addPermission, true)

        permissions[event.server.serverUser]!! +=
            Permission(resetPermission, true)
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

                val user = params.getOrElse(2) { event.sender.id }

                event.pluginMessage("Permissions of $user:")

                permissions[event.server.getUserById(user)]!!.forEach {
                    event.sendMessageToSender("- ${it.action}: ${it.isAllowed}")
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

                val user = params[2]
                val command = params[3]
                val isAllowed = params[4].toBoolean()
                val userPermissions = permissions[event.server.getUserById(user)]!!

                if (userPermissions.any { it.action == command })
                    userPermissions.removeAll { it.action == command }

                userPermissions +=
                    Permission(command, isAllowed)

                event.sendMessageToSender("Added a permission to $user for $command (allowed: $isAllowed)")
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

                val user = params[2]
                val command = params.getOrElse(3, { "" })

                permissions[event.server.getUserById(user)]!!.removeAll {
                    command == "" || it.action == command
                }

                event.pluginMessage("Reset permissions of $user.")
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
