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
 * An abstract Event.
 * 
 * @author Trevor Robinson
 */
public abstract class Event
{
    private String name;

    /**
     * Create a new Event.
     */
    public Event()
    {
        // do nothing
    }

    /**
     * Create a new Event with the specified name.
     *
     * @param name the name of this Event
     */
    public Event(String name)
    {
        this.name = name;
    }

    /**
     * Contains code that should be run prior to waiting on this Event.
     */
    protected void preWait()
    {
        // do nothing by default
    }

    /**
     * Contains code that should be run subsequent to waiting on this Event.
     */
    protected void postWait()
    {
        // do nothing by default
    }

    /**
     * Returns whether or not this Event has occurred.
     *
     * @return <code>true</code> if this Event has occurred, <code>false</code>
     *      otherwise
     */
    public abstract boolean hasOccurred();

    /**
     * Set whether or not this Event has occurred.
     *
     * @param occurred <code>true</code> if this Event has occurred, <code>false</code>
     *      otherwise
     */
    protected abstract void setOccurred(boolean occurred);

    @Override
    public String toString()
    {
        return name != null ? name : super.toString();
    }
}
