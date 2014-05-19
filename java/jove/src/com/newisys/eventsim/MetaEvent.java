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
 * An Event comprised of other Events and whose state depends on the
 * state of its constituent Events.
 * 
 * @author Trevor Robinson
 */
public abstract class MetaEvent
    extends StepEvent
{
    protected final Collection< ? extends Event> events;
    private final boolean autoReset;

    /**
     * Creates a new MetaEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param events a Collection of events that comprise this MetaEvent
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public MetaEvent(Collection< ? extends Event> events, boolean autoReset)
    {
        this(null, events, autoReset);
    }

    /**
     * Creates a new MetaEvent from the collection specified. If <code>autoReset
     * </code> is <code>true</code>, the event will be reset after it has been
     * satisfied.
     *
     * @param name the name of this MetaEvent
     * @param events a Collection of events that comprise this MetaEvent
     * @param autoReset <code>true</code> if this event should be reset after
     *      it has been satisfied, <code>false</code> otherwise
     */
    public MetaEvent(
        String name,
        Collection< ? extends Event> events,
        boolean autoReset)
    {
        super(name);
        if (events.isEmpty())
        {
            throw new IllegalArgumentException("Empty event set for meta-event");
        }
        this.events = events;
        this.autoReset = autoReset;
    }

    /**
     * Gets this MetaEvent's constituent events.
     *
     * @return the Collection of events that comprise this MetaEvent
     */
    final Collection< ? extends Event> getEvents()
    {
        return events;
    }

    /**
     * Returns whether or not this MetaEvent has auto-reset enabled. Auto-reset
     * causes {@link #reset} to be called once this MetaEvent has occurred.
     *
     * @return <code>true</code> if this MetaEvent has auto-reset enabled,
     * <code>false</code> otherwise
     */
    public boolean isAutoReset()
    {
        return autoReset;
    }

    /**
     * Resets the state of this MetaEvent. This includes marking it has not
     * having occurred.
     */
    void reset()
    {
        setOccurred(false);
    }

    /**
     * Contains code that should be run prior to waiting on this MetaEvent.
     * Specifically, it calls {@link Event#preWait} on each of its constituent
     * events.
     */
    @Override
    protected void preWait()
    {
        for (final Event e : events)
        {
            e.preWait();
        }
    }

    /**
     * Contains code that should be run subsequent to waiting on this MetaEvent.
     * Specifically, it calls {@link Event#postWait} on each of its constituent
     * events.
     */
    @Override
    protected void postWait()
    {
        for (final Event e : events)
        {
            e.postWait();
        }
    }

    /**
     * Notify this MetaEvent that <code>e</code> has occurred.
     *
     * @param e the Event that has occurred
     */
    abstract void notifyOf(Event e);
}
