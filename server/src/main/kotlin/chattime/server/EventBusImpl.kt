/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server

import chattime.api.event.Event
import chattime.api.event.EventBus
import chattime.api.event.EventHandler
import chattime.api.event.EventType
import io.reactivex.subjects.PublishSubject

class EventBusImpl : EventBus
{
    private val subject = PublishSubject.create<Event>()

    override fun <E : Event> subscribe(eventType: EventType<E>, block: (E) -> Unit)
    {
        subject.ofType(eventType.eventClass).subscribe(block)
    }

    override fun <E : Event> subscribe(eventType: EventType<E>, handler: EventHandler<E>)
    {
        subject.ofType(eventType.eventClass).subscribe(handler::handle)
    }

    override fun post(event: Event) = subject.onNext(event)
}
