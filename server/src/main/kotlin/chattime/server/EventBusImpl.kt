/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.server

import chattime.server.api.event.Event
import chattime.server.api.event.EventBus
import chattime.server.api.event.EventType
import io.reactivex.subjects.PublishSubject

class EventBusImpl : EventBus
{
    private val subject = PublishSubject.create<Event>()

    override fun <E : Event> subscribe(eventType: EventType<E>, block: (E) -> Unit)
    {
        subject.ofType(eventType.eventClass).subscribe(block)
    }

    override fun post(event: Event) = subject.onNext(event)
}