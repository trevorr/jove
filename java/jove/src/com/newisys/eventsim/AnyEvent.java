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

/**
 * A MetaEvent which occurs once any of its constituent events have occurred.
 * 
 * @author Trevor Robinson
 */
public final class AnyEvent
    extends MetaEvent
{
    /**
     * Creates a new AnyEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param events a Collection of events that can occur for this AnyEvent to
     *      be satisfied
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public AnyEvent(Collection< ? extends Event> events, boolean autoReset)
    {
        this("AnyEvent" + events, events, autoReset);
    }

    /**
     * Creates a new AnyEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param name the name of this AnyEvent
     * @param events a Collection of events that can occur for this AnyEvent to
     *      be satisfied
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public AnyEvent(
        String name,
        Collection< ? extends Event> events,
        boolean autoReset)
    {
        super(name, events, autoReset);
    }

    /**
     * Return whether or not this AnyEvent has occurred. An AnyEvent is
     * considered to have occurred when any of its constituent events has occurred.
     *
     * @return <code>true</code> if this AnyEvent has occurred, <code>false</code>
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
            if (e.hasOccurred())
            {
                setOccurred(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Notify this AnyEvent that <code>e</code> has occurred.
     *
     * @param e the Event that has occurred
     */
    @Override
    void notifyOf(Event e)
    {
        assert (events.contains(e));
        setOccurred(true);
    }
}
