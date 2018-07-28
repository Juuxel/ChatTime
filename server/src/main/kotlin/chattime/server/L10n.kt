/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server

import chattime.api.util.Localization
import java.text.MessageFormat
import java.util.*

object L10n : Localization
{
    private val resourceBundle: ResourceBundle = ResourceBundle.getBundle("chattime.l10n.L10n")

    override operator fun get(key: String): String
        = if (resourceBundle.containsKey(key)) resourceBundle.getString(key)
          else key

    override operator fun get(key: String, vararg args: Any?): String
    {
        val string = this[key]

        if (string == key)
            return key

        return MessageFormat.format(string, *args.map {
            if (it is Number)
                it.toString()
            else
                it
        }.toTypedArray())
    }
}
