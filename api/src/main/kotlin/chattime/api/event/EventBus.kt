/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.api.event

import java.util.function.Consumer

/**
 * A simple event bus which distributes [Event] objects
 * to event handlers.
 */
interface EventBus
{
    /**
     * Subscribes to the events of [eventType]
     * and calls [block] when an event of the type
     * is received.
     *
     * @param E the generic event type
     * @param eventType the [EventType] object representing [E]
     * @param block the event handler function
     */
    fun <E : Event> subscribe(eventType: EventType<E>,
                              block: (E) -> Unit)

    /**
     * A Java compatibility version (no function types) of
     * `EventBus.subscribe`.
     *
     * @param E the generic event type
     * @param eventType the [EventType] object representing [E]
     * @param handler the event handler
     *
     * @see EventBus.subscribe
     */
    fun <E : Event> subscribe(eventType: EventType<E>,
                              handler: EventHandler<E>)

    /**
     * Posts [event] to the event bus and calls
     * subscribed event handlers with the event.
     *
     * @param event the event
     */
    fun post(event: Event)
}
