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
         * Constructs a simple [Command] object from a name,
         * a description and a function.
         *
         * @param name the command name
         * @param desc the command description
         * @param block the command function
         *
         * @return a command
         */
        fun construct(name: String, desc: String,
                      block: (MessageEvent) -> Unit): Command
        = SimpleCommand(name, desc, block)

        private class SimpleCommand(override val name: String,
                                    override val description: String,
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
         * @param joinLastParam if true, counts all of the text in the end as
         *                      the last parameter
         * @param lastParamIndex the last parameter's index for [joinLastParam]
         *
         * @throws IllegalArgumentException if [msg] is not a command
         * @return a list of command parameters
         */
        fun getCommandParams(msg: String,
                             joinLastParam: Boolean = false,
                             lastParamIndex: Int = -1): List<String>
        {
            if (!msg.startsWith("!") || msg == "!")
                throw IllegalArgumentException("'$msg' is not a command!")

            val hasSpace = msg.indexOf(' ') != -1

            return if (hasSpace)
            {
                val split = msg.substring(1).split(' ')

                if (joinLastParam)
                {
                    val firstPart = split.subList(0, lastParamIndex)
                    val secondPart = split.subList(lastParamIndex, split.lastIndex + 1)

                    firstPart + secondPart.joinToString(" ")
                }
                else split
            }
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
         * The command description (used by [DefaultCommands.HELP]).
         */
        val description: String

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
         *   - If `<command name>` is not set,
         *     lists all commands and their descriptions.
         *   - If it is set, sends a long description about the command.
         */
        HELP("help"),

        /**
         * Sends a message containing the sender's user id.
         * An optional parameter can be set to display another user's id.
         *
         * - Usage: `!id [<user name> or sender]`
         *   - If `<user name>` is set, lists all users with it
         *     in their name.
         *   - Otherwise sends a message containing the sender's user id.
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
         * Sends messages to the sender containing each user's name and id.
         */
        LIST_USERS("users"),

        /**
         * Sends a message to the sender containing a user name.
         *
         * - Usage: `!who-is <user id>`
         *   - Sends a message containing the user name of `<user id>`
         */
        WHO_IS("who-is"),

        /**
         * Sends a message visible to only one user.
         *
         * - Usage: `!pm <user id> <msg>`
         *   - Sends a message to `<user id>` containing `<msg>`
         */
        PRIVATE_MESSAGE("pm")
    }
}
