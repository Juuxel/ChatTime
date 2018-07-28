/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.plugin

/**
 * Represents an entry in the plugin load order list.
 *
 * @constructor The primary constructor.
 *
 * @property id the other plugin
 * @property isRequired set to true if the other plugin is required
 */
sealed class LoadOrder(
    val id: String,
    val isRequired: Boolean)
{
    /**
     * Loads this plugin before the other plugin.
     *
     * @constructor The primary constructor.
     *
     * @param id the other plugin
     * @param isRequired set to true if the other plugin is required
     */
    class Before(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)

    /**
     * Loads this plugin after the other plugin.
     *
     * @constructor The primary constructor.
     *
     * @param id the other plugin
     * @param isRequired set to true if the other plugin is required
     */
    class After(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
}
