package chattime.client


fun main(args: Array<String>)
{
    if (args.isNotEmpty() && args[0] == "cli")
        cliStart(args.copyOfRange(1, args.size))
    else
        guiStart(args)
}
