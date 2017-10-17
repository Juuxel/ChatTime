/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.features

import chattime.api.User
import chattime.api.plugin.LoadOrder
import chattime.api.plugin.LoadOrder.After
import chattime.api.plugin.Plugin

/**
 * Permissions is a feature plugin for forbidding and allowing
 * the use of different user actions, such as commands.
 *
 * The server's console user ([chattime.api.Server.serverUser]) has
 * all permissions by default, and they're non-removable.
 *
 * ## The `!permissions` command
 *
 * - Permission adds a new command, `permissions`.
 * - By default the use of `add` and `reset` is forbidden
 *   - Allowed for the server user
 *   - `list` is allowed by default
 * - Usage: `!permissions <subcommand: list, add, remove>`
 *   - `list <user id>`: Lists the permission of `<user id>`
 *   - `add <user id> <command name> <type>`: Adds a new permission of `<type>`
 *     to `<user id>` for using `<command name>`
 *     - Type is either `Allow` or `Forbid` (case ignored)
 *     - Replaces any previous permissions for `<command name>`
 *     - The user can give only permissions that they have themselves
 *   - `reset <user id> [<command name>]`: Resets all permissions
 *     for `<command name>` from `<user id>`
 *     - If `<command name>` is not set, resets all permissions
 *
 * ## Command permissions
 *
 * The names of command permissions are `command.{command name}`.
 * For subcommands this pattern is used: `command.{command name}.{subcommand}`.
 */
interface Permissions : Plugin
{
    override val id: String
        get() = "Permissions"

    // Commands is required providing !permissions
    override val loadOrder: List<LoadOrder>
        get() = listOf(After("Commands", isRequired = true))

    /**
     * Makes [permission] the default permission for its action.
     *
     * If no global permission is set for an action,
     * it will be allowed.
     *
     * @param permission the permission
     */
    fun addGlobalPermission(permission: Permission)

    /**
     * Checks if [user] has the permission to use [action],
     * or a matching global permission for it.
     *
     * @param user the user
     * @param action the action
     * @return true if [user] can use [action], false otherwise
     */
    fun hasPermission(user: User, action: String): Boolean

    /**
     * A permission applying to [action].
     *
     * @constructor The primary constructor.
     *
     * @param action the action
     * @param isAllowed is the action allowed
     */
    data class Permission(
        /** The action. */
        val action: String,
        /** True if the action is allowed. */
        val isAllowed: Boolean)
    {
        override fun equals(other: Any?): Boolean
            = other is Permission && other.action == action

        override fun hashCode(): Int = action.hashCode()
    }
}
