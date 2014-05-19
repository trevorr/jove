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

/**
 * An Event that does not retain its state as on or off.
 * 
 * @author Trevor Robinson
 */
public class PulseEvent
    extends Event
{
    /**
     * Create a new PulseEvent.
     */
    public PulseEvent()
    {
        super();
    }

    /**
     * Create a new PulseEvent with the specified name.
     *
     * @param name the name of this PulseEvent
     */
    public PulseEvent(String name)
    {
        super(name);
    }

    /**
     * Returns whether or not this PulseEvent has occurred. PulseEvents do not
     * retain on/off state and thus always return <code>false</code>.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean hasOccurred()
    {
        return false;
    }

    /**
     * Set whether or not this PulseEvent has occurred. PulseEvents do not
     * maintain on/off state and thus this method does nothing.
     *
     * @param occurred <code>true</code> if this StepEvent has occurred,
     *      <code>false</code> otherwise
     */
    @Override
    protected void setOccurred(boolean occurred)
    {
        // do nothing
    }
}
