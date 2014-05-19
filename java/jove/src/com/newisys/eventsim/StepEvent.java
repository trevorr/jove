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
 * An Event that retains its state as on or off.
 * <P>
 * A StepEvent is turned on by calling <code>hasOccurred(true)</code> and maintains
 * that state until <code>hasOccurred(false)</code> is called.
 * 
 * @author Trevor Robinson
 */
public class StepEvent
    extends Event
{
    private boolean occurred;

    /**
     * A StepEvent that is always on.
     */
    public static final StepEvent ALWAYS = new StepEvent("ALWAYS", true);

    /**
     * A StepEvent that is always off.
     */
    public static final StepEvent NEVER = new StepEvent("NEVER", false);

    /**
     * Create a new StepEvent.
     */
    public StepEvent()
    {
        super();
    }

    /**
     * Create a new StepEvent with the specified name.
     *
     * @param name the name of this StepEvent
     */
    public StepEvent(String name)
    {
        super(name);
    }

    /**
     * Create a new StepEvent with the specified name and initial state.
     *
     * @param name the name of this StepEvent
     * @param occurred a boolean specifying whether or not this StepEvent is on
     */
    private StepEvent(String name, boolean occurred)
    {
        super(name);
        this.occurred = occurred;
    }

    /**
     * Returns whether or not this StepEvent has occurred.
     *
     * @return <code>true</code> if this StepEvent has occurred, <code>false</code>
     *      otherwise
     */
    @Override
    public boolean hasOccurred()
    {
        return occurred;
    }

    /**
     * Set whether or not this StepEvent has occurred.
     *
     * @param occurred <code>true</code> if this StepEvent has occurred,
     *      <code>false</code> otherwise
     */
    @Override
    protected void setOccurred(boolean occurred)
    {
        this.occurred = occurred;
    }
}
