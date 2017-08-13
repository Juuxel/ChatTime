package chattime.client

// Just a little special main function so I can provide the GUI as a default...

fun main(args: Array<String>)
{
    if (args.isNotEmpty() && args[0] == "cli")
        cliStart(args.copyOfRange(1, args.size))
    else
        guiStart(args)
}
