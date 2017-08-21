package chattime.client

import chattime.client.gui.guiStart

// Just a little special main function so I can provide the GUI as a default and CLI as an option :-D

fun main(args: Array<String>)
{
    if (args.isNotEmpty() && args[0] == "cli")
        cliStart(args.copyOfRange(1, args.size))
    else
        guiStart(args)
}
