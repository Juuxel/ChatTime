/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.util;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.*;

public final class JavaHelper
{
    private JavaHelper() throws Exception { throw new Exception("JavaHelper can not be constructed."); }

    /**
     * This method is used to call from Kotlin without errors.
     *
     * @see picocli.CommandLine#parse(String...)
     */
    public static List<CommandLine> picocliParse(@NotNull CommandLine commandLine, String[] args)
    {
        return commandLine.parse(args);
    }
}
