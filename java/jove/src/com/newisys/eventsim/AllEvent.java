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
 * A MetaEvent which occurs once all of its constituent events have occurred.
 * 
 * @author Trevor Robinson
 */
public final class AllEvent
    extends MetaEvent
{
    protected final Collection<Event> remainingEvents;

    /**
     * Creates a new AllEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param events a Collection of events that must occur for this AllEvent to
     *      be satisfied
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public AllEvent(Collection< ? extends Event> events, boolean autoReset)
    {
        this("AllEvent" + events, events, autoReset);
    }

    /**
     * Creates a new AllEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param name the name of this AllEvent
     * @param events a Collection of events that must occur for this AllEvent to
     *      be satisfied
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public AllEvent(
        String name,
        Collection< ? extends Event> events,
        boolean autoReset)
    {
        super(name, events, autoReset);
        remainingEvents = new LinkedList<Event>(events);
    }

    /**
     * Return whether or not this AllEvent has occurred. An AllEvent is
     * considered to have occurred when each of its constituent events has occurred.
     *
     * @return <code>true</code> if this AllEvent has occurred, <code>false</code>
     *      otherwise
     */
    @Override
    public boolean hasOccurred()
    {
        // check flag first
        if (super.hasOccurred()) return true;

        // check underlying events, in case they occurred silently (without a
        // call to MetaEvent.notifyOf())
        for (final Event e : events)
        {
            if (!e.hasOccurred()) return false;
        }
        setOccurred(true);
        return true;
    }

    /**
     * Reset this AllEvent. All constituent events are marked as having not
     * occurred.
     */
    @Override
    void reset()
    {
        // clear flag
        super.reset();

        // reset remaining event set
        remainingEvents.clear();
        remainingEvents.addAll(events);
    }

    /**
     * Notify this AllEvent that <code>e</code> has occurred.
     *
     * @param e the Event that has occurred
     */
    @Override
    void notifyOf(Event e)
    {
        assert (events.contains(e));
        synchronized (remainingEvents)
        {
            remainingEvents.remove(e);
            if (remainingEvents.isEmpty())
            {
                setOccurred(true);
            }
        }
    }
}
