/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client

import chattime.common.Info
import picocli.CommandLine
import picocli.CommandLine.*

fun main(args: Array<String>)
{
    val params = Params()
    val commandLine = CommandLine(params)

    try
    {
        commandLine.parseWithHandler(RunLast(), args)
    }
    catch (e: ParameterException)
    {
        System.err.println("error: ${e.message}")
    }
    catch (e: Exception)
    {
        System.err.print("unknown error:")
        e.printStackTrace()
    }
}

@Command(name = "chattime-client",
         version = [Info.fullVersion],
         subcommands = [HelpCommand::class, CliClient::class, GuiClient::class])
internal class Params : Runnable
{
    override fun run() = GuiClient().run()
}
