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

package com.newisys.ova;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.eventsim.Event;
import com.newisys.eventsim.PulseEvent;

/**
 * Class to help manage OVA events from Jove.
 * <P>
 * <b>This class is not yet supported</b>
 * 
 * @param <K> the type of enumeration used by this OVAEvent
 * @author Jon Nall
 */
abstract public class OVAEvent<K extends OVAEventType>
    extends PulseEvent
{
    private boolean isBound;
    private K eventType;
    private List<OVAEvent> eventList;
    private Iterator<OVAEvent> eventIter;

    /**
     * Create an OVA Event associated with the type of event described by the
     * event parameter.
     * <P>
     *
     * @param eventType the event type to associate with this OVAEvent
     */
    public OVAEvent(K eventType)
    {
        this.isBound = false;
        this.eventType = eventType;
        this.eventList = new LinkedList<OVAEvent>();
    }

    @Override
    protected final void preWait()
    {
        // check that this Event is bound to either the OVAEngine or an
        // OVAAssert
        if (!isBound)
        {
            throw new OVAException(
                "Cannot call waitOnEvent() on an event that is not "
                    + "bound to either an OVAEngine or OVAAssert");
        }
    }

    @Override
    protected final void postWait()
    {
        // the OVAEngine should assign the event(s) that woke this thread up
        // into eventList before notifying on the event.
        eventIter = eventList.iterator();
    }

    /**
     * Returns the event type that unblocked this thread. If more than one event
     * caused the thread to unblock, this method may be called multiple times,
     * until <code>null</code> is returned.
     * <P>
     * If there are multiple calls to
     * {@link com.newisys.dv.DVSimulation#waitFor(Event)} without intervening
     * calls to getNextEvent, getNextEvent will return the set of event types
     * corresponding to the last call to
     * {@link com.newisys.dv.DVSimulation#waitFor(Event)}.
     *
     * @return the event type that unblocked this thread or
     *      <code>null</code> if there are no more events.
     */
    public OVAEvent getNextEvent()
    {
        if (eventIter.hasNext())
        {
            return eventIter.next();
        }
        else
        {
            return null;
        }
    }

    /**
     * Called by OVAEngine or OVAAssert once the OVAEvent has been bound to one
     * of the two. Calling this means that it's ok for someone to wait on this
     * OVAEvent via {@link com.newisys.dv.DVSimulation#waitFor(Event)}.
     */
    void bind()
    {
        isBound = true;
    }

    /**
     * Called by OVAEngine or OVAAssert once the OVAEvent has become unbound to
     * one of the two. Calling this means that it's not ok for someone to wait
     * on this event via {@link com.newisys.dv.DVSimulation#waitFor(Event)}.
     */
    void unbind()
    {
        isBound = false;
    }

    /**
     * Get the event type associated with this OVAEvent.
     *
     * @return the event type
     */
    K getEventType()
    {
        return eventType;
    }
}
