/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.util

interface Localization
{
    operator fun get(key: String): String
    operator fun get(key: String, vararg args: Any?): String
}
