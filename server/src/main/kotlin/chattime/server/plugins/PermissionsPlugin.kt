/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.plugins

import chattime.api.User
import chattime.api.event.*
import chattime.api.features.Commands
import chattime.api.plugin.Plugin

class PermissionsPlugin : Plugin
{
    private val permissions: HashMap<User, ArrayList<Permission>>
    = HashMap()

    private val commandDefaults: HashMap<String, PermissionType>
    = HashMap()

    override val id = "Permissions"

    override fun load(event: PluginLoadEvent)
    {
        event.eventBus.subscribe(EventType.commandCall) { handleCommand(it) }
        event.eventBus.subscribe(EventType.userJoin) { handleUserJoin(it) }
        event.eventBus.subscribe(EventType.pluginMessage) {
            if (it.msg is Permission)
            {
                val perm = it.msg as Permission
                commandDefaults[perm.commandName] = perm.type
            }
        }

        event.server.commandsPlugin.addCommand(
            Commands.construct("permissions", "Handle permissions.") {
                permissionCommand(it)
            }
        )

        commandDefaults["permissions"] = PermissionType.FORBID

        permissions[event.server.serverUser] = ArrayList()
        permissions[event.server.serverUser]!! +=
            Permission("permissions", PermissionType.ALLOW)
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
        (commandDefaults[event.commandName] == PermissionType.FORBID
            && !anyPerm(PermissionType.ALLOW)) // No allows for forbidden cmd
            || anyPerm(PermissionType.FORBID)) // Forbids for regular cmd
        {
            event.sendMessageToSender("Usage of !${event.commandName} denied.")
            event.cancel()
        }
    }

    private fun permissionCommand(event: MessageEvent)
    {
        val params = Commands.getCommandParams(event.msg)

        if (params.size < 2)
        {
            event.sendMessageToSender("Usage: !permissions <subcommand: list, add, remove>")
            return
        }

        when (params[1])
        {
            "list" -> {
                // TODO Implement
            }

            "add" -> {
                val user = params[2]
                val command = params[3]
                val type = PermissionType.valueOf(params[4].toUpperCase())

                permissions[event.server.getUserById(user)]!! +=
                    Permission(command, type)

                event.sendMessageToSender("Added a $type permission to $user for $command")
            }

            "remove" -> {
                val user = params[2]
                val command = params[3]

                permissions[event.server.getUserById(user)]!!.removeAll {
                    it.commandName == command
                }

                event.sendMessageToSender("Removed permissions for $command from $user")
            }
        }
    }

    class Permission(val commandName: String, val type: PermissionType)

    enum class PermissionType
    { ALLOW, FORBID }
}