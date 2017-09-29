/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.api.event

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
     * Posts [event] to the event bus and calls
     * subscribed event handlers with the event.
     *
     * @param event the event
     */
    fun post(event: Event)
}
