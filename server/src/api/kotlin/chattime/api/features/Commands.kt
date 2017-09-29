/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api.features

import chattime.api.event.MessageEvent
import chattime.api.plugin.Plugin

/**
 * Commands is a feature plugin for using chat commands.
 *
 * @see DefaultCommands for a list of the default commands.
 */
interface Commands : Plugin
{
    override val id
        get() = "Commands"

    /**
     * A list of the commands that are loaded into Commands.
     */
    val commands: List<Command>

    companion object
    {
        /**
         * Constructs a simple [Command] object from a name and a function.
         */
        fun construct(name: String,
                      block: (MessageEvent) -> Unit): Command
        = SimpleCommand(name, block)

        private class SimpleCommand(override val name: String,
                                    private val block: (MessageEvent) -> Unit) : Command
        {
            override fun handleMessage(event: MessageEvent)
            {
                block(event)
            }
        }

        /**
         * Splits the command into a list of parameters.
         *
         * @param msg the chat message with the command
         *
         * @throws IllegalArgumentException if [msg] is not a command
         * @return a list of command parameters
         */
        fun getCommandParams(msg: String): List<String>
        {
            if (!msg.startsWith("!") || msg == "!")
                throw IllegalArgumentException("'$msg' is not a command!")

            val hasSpace = msg.indexOf(' ') != -1

            return if (hasSpace) msg.substring(1).split(' ')
            else listOf(msg.substring(1))
        }
    }

    /**
     * Adds [command] to [commands].
     * When a command is added, it can be called
     * by sending a message starting with `!`
     * and the command name.
     *
     * @param command the command
     */
    fun addCommand(command: Command)

    /**
     * A chat command.
     */
    interface Command
    {
        /**
         * The command name (used when calling).
         */
        val name: String

        /**
         * Handles the message which contains the command call.
         *
         * @param event the message event containing the message
         */
        fun handleMessage(event: MessageEvent)
    }

    /**
     * A list of the default commands.
     */
    enum class DefaultCommands(
        /** The command name. */
        val commandName: String)
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
