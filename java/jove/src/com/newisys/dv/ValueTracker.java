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

package com.newisys.dv;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Maintains a sliding window of value change events for a fixed number of
 * discrete time ticks in the past. This class is used by InputMonitor to
 * provide input values at clock edges minus the input skew.
 * 
 * @author Trevor Robinson
 */
final class ValueTracker
{
    private final LinkedList<ValueChangeRecord> valueChanges;
    private long maxAge;
    private long curTime;

    /**
     * Constructs a new ValueTracker that tracks value changes for the given
     * number of ticks in the past.
     *
     * @param maxAge number of ticks to keep value changes for
     */
    public ValueTracker(long maxAge)
    {
        setMaxAge(maxAge);
        curTime = Long.MIN_VALUE;
        valueChanges = new LinkedList<ValueChangeRecord>();
        valueChanges.addLast(new ValueChangeRecord(curTime, null));
    }

    /**
     * Returns the number of ticks in the past that value changes are kept for.
     *
     * @return number of ticks to keep value changes for
     */
    public long getMaxAge()
    {
        return maxAge;
    }

    /**
     * Sets the number of ticks in the past that value changes are kept for.
     *
     * @param maxAge number of ticks to keep value changes for
     */
    public void setMaxAge(long maxAge)
    {
        if (maxAge < 0)
        {
            throw new IllegalArgumentException(
                "Maximum age must be greater than or equal to zero");
        }
        this.maxAge = maxAge;
    }

    /**
     * Returns the current simulation time from the point of view of this value
     * tracker, which is the time of the last value change event.
     *
     * @return the current time
     */
    public long getCurTime()
    {
        return curTime;
    }

    /**
     * Tracks the given value occurring at the given time. The given time must
     * be greater than or equal to the current time. If the given time is
     * greater, it becomes the current time.
     *
     * @param simTime the simulation tick the value change occurred in
     * @param value the new value at that time
     */
    public void trackChange(long simTime, Object value)
    {
        if (simTime > curTime)
        {
            // time has advanced; add a new record and update the current time
            valueChanges.addLast(new ValueChangeRecord(simTime, value));
            updateCurTime(simTime);
        }
        else if (simTime == curTime)
        {
            // time has not advanced since the last call to trackChange() or
            // getValue(); if the last value change record is for this tick,
            // just update its value; otherwise, add a new record
            ValueChangeRecord rec = valueChanges.getLast();
            if (rec.simTime == simTime)
            {
                rec.value = value;
            }
            else
            {
                assert (simTime > rec.simTime);
                valueChanges.addLast(new ValueChangeRecord(simTime, value));
            }
        }
        else
        {
            throw new IllegalArgumentException(
                "Value change time must be greater than or equal to current time");
        }
    }

    /**
     * Returns the value tracked by this object a given number of ticks in the
     * past. The number of ticks must be less than or equal to the maximum age
     * set for this value tracker. The given simulation time is also used to
     * update the current simulation time maintained by this tracker.
     *
     * @param simTime the current simulation time
     * @param age the number of ticks in the past to retrieve the value for
     * @return the value at the given point in time (simTime - age)
     */
    public Object getValue(long simTime, long age)
    {
        // validate age
        if (age < 0)
        {
            throw new IllegalArgumentException(
                "Age must be greater than or equal to zero");
        }
        else if (age > maxAge)
        {
            throw new IllegalArgumentException(
                "Age exceeds maximum age tracked");
        }

        // update simulation time
        if (simTime > curTime)
        {
            updateCurTime(simTime);
        }
        else if (simTime < curTime)
        {
            throw new IllegalArgumentException(
                "Value request time must be greater than or equal to current time");
        }

        // scan for relevant value change record
        long searchTime = simTime - age;
        int recCount = valueChanges.size();
        assert (recCount > 0);
        ListIterator<ValueChangeRecord> iter = valueChanges
            .listIterator(recCount);
        while (iter.hasPrevious())
        {
            ValueChangeRecord rec = iter.previous();
            if (rec.simTime <= searchTime)
            {
                return rec.value;
            }
        }

        // maximum tracking age must have been increased, but the corresponding
        // amount of history has not been accrued
        throw new IllegalStateException(
            "Value change record is not available for the requested time");
    }

    /**
     * Updates the current simulation time from the point of view of this value
     * tracker. This method also removes value change records exceeding the
     * maximum age for this tracker.
     *
     * @param simTime the new simulation time
     */
    private void updateCurTime(long simTime)
    {
        curTime = simTime;

        // if the second record is adequate to cover values in the maximum age
        // for this tracker, remove the first record
        while (valueChanges.size() >= 2)
        {
            ValueChangeRecord rec = valueChanges.get(1);
            if (curTime - rec.simTime >= maxAge)
            {
                valueChanges.removeFirst();
            }
            else
            {
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "curTime=" + curTime + "; maxAge=" + maxAge + "; valueChanges="
            + valueChanges;
    }

    private static final class ValueChangeRecord
    {
        final long simTime;
        Object value;

        public ValueChangeRecord(long simTime, Object value)
        {
            this.simTime = simTime;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value + " @ " + simTime;
        }
    }
}
