/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.event

/**
 * An event handler interface for Java compatibility. (Prevents weird
 * FunctionX types from appearing in your code.)
 */
@FunctionalInterface
interface EventHandler<in E : Event>
{
    /**
     * Handle an event.
     *
     * @param event the event
     */
    fun handle(event: E)
}
