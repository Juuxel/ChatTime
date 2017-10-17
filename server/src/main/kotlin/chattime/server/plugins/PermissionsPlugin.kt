/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
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

    private val globals: HashMap<String, PermissionType>
    = HashMap()

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

        globals["permissions"] = PermissionType.FORBID

        permissions[event.server.serverUser] = ArrayList()
        permissions[event.server.serverUser]!! +=
            Permission("permissions", PermissionType.ALLOW)
    }

    override fun addGlobalPermission(permission: Permission)
    {
        globals[permission.commandName] = permission.type
    }

    private fun handleUserJoin(event: UserJoinEvent)
    {
        permissions[event.user] = ArrayList()
    }

    private fun handleCommand(event: CommandEvent)
    {
        val userPerms = permissions[event.sender] ?: return

        fun anyPerm(type: PermissionType): Boolean
            = userPerms.any {
            it.commandName == event.commandName
            && it.type == type
        }

        if (
        (globals[event.commandName] == PermissionType.FORBID
            && !anyPerm(PermissionType.ALLOW)) // No allows for forbidden cmd
            || anyPerm(PermissionType.FORBID)) // Forbids for regular cmd
        {
            event.pluginMessage("Usage of !${event.commandName} denied.")
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
                val user = params.getOrElse(2) { event.sender.id }

                event.pluginMessage("Permissions of $user:")

                permissions[event.server.getUserById(user)]!!.forEach {
                    event.sendMessageToSender("- ${it.commandName}: ${it.type}")
                }
            }

            "add" -> {
                if (params.size < 5)
                {
                    event.pluginMessage("Usage: !permissions add <user> <command> <type: allow, forbid>")
                    return
                }

                val user = params[2]
                val command = params[3]
                val type = PermissionType.valueOf(params[4].toUpperCase())
                val userPermissions = permissions[event.server.getUserById(user)]!!

                if (userPermissions.any { it.commandName == command })
                    userPermissions.removeAll { it.commandName == command }

                userPermissions +=
                    Permission(command, type)

                event.sendMessageToSender("Added a $type permission to $user for $command")
            }

            "reset" -> {
                if (params.size < 3)
                {
                    event.pluginMessage("Usage: !permissions reset <user> [<command>]")
                    return
                }

                val user = params[2]
                val command = params.getOrElse(3, { "" })

                permissions[event.server.getUserById(user)]!!.removeAll {
                    command == "" || it.commandName == command
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
}
