/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api.features

import chattime.api.plugin.Plugin

/**
 * Features are special plugins that are defined in this package.
 * They are simplified specifications of the core plugins
 * bundled with the server.
 *
 * @constructor The primary constructor.
 *
 * @param P the feature plugin type
 * @param id the feature plugin id
 *
 * @see Commands
 */
class Features<P : Plugin> private constructor(
    /** The feature plugin id. */
    val id: String)
{
    companion object
    {
        /**
         * The [Commands] feature.
         */
        val commands = Features<Commands>("Commands")
    }
}
