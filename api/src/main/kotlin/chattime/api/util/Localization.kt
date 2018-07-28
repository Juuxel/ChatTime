/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.util

/**
 * Provides localized strings for the current locale.
 */
interface Localization
{
    /**
     * Gets a localized string with the [key].
     *
     * Returns the [key] if no matching string is found.
     */
    operator fun get(key: String): String

    /**
     * Gets a localized string with the [key] and formats
     * it with the format [args].
     *
     * Formatting uses `java.text.MessageFormat`'s positional arguments:
     * `{0}` for the first argument, `{1}` for the second argument, etc.
     *
     * Follows the behavior of `get(String)` if no matching
     * string for the [key] is found.
     */
    operator fun get(key: String, vararg args: Any?): String
}
