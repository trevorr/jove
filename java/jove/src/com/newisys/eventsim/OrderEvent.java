/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.eventsim;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A MetaEvent which occurs once all of its constituent events have occurred,
 * in the order in which they are specified.
 * 
 * @author Trevor Robinson
 */
public final class OrderEvent
    extends MetaEvent
{
    protected final LinkedList<Event> remainingEvents;

    /**
     * Creates a new OrderEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param events a Collection of events that must occur for this OrderEvent to
     *      be satisfied.
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public OrderEvent(Collection<Event> events, boolean autoReset)
    {
        this("OrderEvent" + events, events, autoReset);
    }

    /**
     * Creates a new OrderEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param name the name of this OrderEvent
     * @param events a Collection of events that must occur for this OrderEvent to
     *      be satisfied
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public OrderEvent(String name, Collection<Event> events, boolean autoReset)
    {
        super(name, events, autoReset);
        remainingEvents = new LinkedList<Event>(events);
    }

    /**
     * Reset this OrderEvent. All constituent events are marked as having not
     * occurred.
     */
    @Override
    void reset()
    {
        // clear flag
        super.reset();

        // reset remaining event list
        remainingEvents.clear();
        remainingEvents.addAll(events);
    }

    /**
     * Notify this OrderEvent that <code>e</code> has occurred.
     *
     * @param e the Event that has occurred
     */
    @Override
    void notifyOf(Event e)
    {
        assert (events.contains(e));
        synchronized (remainingEvents)
        {
            if (!remainingEvents.isEmpty())
            {
                final Event nextEvent = remainingEvents.getFirst();
                if (e.equals(nextEvent))
                {
                    remainingEvents.removeFirst();
                    if (remainingEvents.isEmpty())
                    {
                        setOccurred(true);
                    }
                }
            }
        }
    }
}
