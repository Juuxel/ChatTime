/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.*;

public final class JavaHelper
{
    private JavaHelper() throws Exception { throw new Exception("JavaHelper can not be constructed."); }

    /**
     * This method is used to call from Kotlin without errors.
     *
     * @see CommandLine#parse(String...)
     */
    public static List<CommandLine> picocliParse(@NotNull CommandLine commandLine, String[] args)
    {
        return commandLine.parse(args);
    }
}
