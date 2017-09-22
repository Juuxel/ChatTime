/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.features

import chattime.server.api.event.MessageEvent
import chattime.server.api.plugin.Plugin

/**
 * Commands is a feature plugin for using chat commands.
 *
 * @see DefaultCommands for a list of the default commands.
 */
interface Commands : Plugin
{
    override val id
        get() = "Commands"

    fun addCommand(command: Command)

    interface Command
    {
        val commandName: String

        fun handleMessage(event: MessageEvent)
    }

    enum class DefaultCommands(val commandName: String)
    {
        /**
         * Provides info about commands.
         *
         * - Usage: `!help [<command name>]`
         *   - If `<command name>` is not set, lists all commands.
         *   - Otherwise sends information about the command.
         */
        HELP("help"),

        /**
         * Sends a message containing the sender's user ID.
         * An optional parameter can be set to display another user's ID.
         *
         * - Usage: `!id [<user name> or sender]`
         *   - If `<user name>` is set, shows the specified user's ID.
         *     If there are multiple users with the same name, lists all users
         *     and their IDs.
         *   - Otherwise sends a message containing the sender's user ID.
         */
        USER_ID("id"),

        /**
         * Renames the sender.
         *
         * - Usage: `!rename <new name>`
         *   - Sets the sender's name to `<new name>`.
         */
        RENAME_USER("rename"),

        /**
         * Sends messages to the sender listing each plugin on the server.
         */
        LIST_PLUGINS("plugins"),

        /**
         * Sends messages to the sender containing each user's name and ID.
         */
        LIST_USERS("users")
    }
}