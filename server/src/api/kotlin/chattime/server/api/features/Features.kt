/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server.api.features

import chattime.server.api.plugin.Plugin

/**
 * Features are special plugins that are defined in this package.
 * They are simplified specifications of the core plugins
 * bundled with the server.
 *
 * @see Commands
 */
class Features<P : Plugin> private constructor(val id: String)
{
    companion object
    {
        val commands = Features<Commands>("Commands")
    }
}
