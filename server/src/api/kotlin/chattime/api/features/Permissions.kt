/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.features

import chattime.api.plugin.Plugin

/**
 * Permissions is a feature plugin for forbidding and allowing use of commands.
 *
 * ## The `!permissions` command
 *
 * - Permission adds a new command, `permissions`.
 * - Default permission: [PermissionType.FORBID]
 *   - [PermissionType.ALLOW] for the server user
 * - Usage: `!permissions <subcommand: list, add, remove>`
 *   - `list <user id>`: Lists the permission of `<user id>`
 *   - `add <user id> <command name> <type>`: Adds a new permission of `<type>`
 *     to `<user id>` for using `<command name>`
 *     - Type is either `Allow` or `Forbid` (case ignored)
 *     - Replaces any previous permissions for `<command name>`
 *   - `reset <user id> [<command name>]`: Resets all permissions
 *     for `<command name>` from `<user id>`
 *     - If `<command name>` is not set, resets all permissions
 */
interface Permissions : Plugin
{
    override val id: String
        get() = "Permissions"

    /**
     * Makes [permission] the default permission for its command.
     *
     * If no global permission is set for a command,
     * it will be [PermissionType.ALLOW].
     *
     * @param permission the permission
     */
    fun addGlobalPermission(permission: Permission)

    /**
     * A permission applying to the use of [commandName].
     *
     * @constructor The primary constructor.
     *
     * @param commandName the command name
     * @param type the [PermissionType]
     */
    class Permission(
        /** The command name. */
        val commandName: String,
        /** The command type. */
        val type: PermissionType)

    /**
     * A permission type, either [ALLOW] or [FORBID].
     */
    enum class PermissionType
    {
        /**
         * Allows the use of a command,
         * which may be forbidden otherwise by a global permission.
         *
         * @see Permissions.addGlobalPermission
         */
        ALLOW,

        /** Forbids the use of a command. */
        FORBID
    }
}
