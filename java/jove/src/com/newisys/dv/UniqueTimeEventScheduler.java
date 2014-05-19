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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Schedules and steps through events that occur at unique points in time.
 * 
 * @author Trevor Robinson
 */
final class UniqueTimeEventScheduler<T extends EventRecord>
{
    private final EventRecordFactory< ? extends T> factory;
    private final LinkedList<T> events;
    private long curTime;

    public UniqueTimeEventScheduler(EventRecordFactory< ? extends T> factory)
    {
        this.factory = factory;
        events = new LinkedList<T>();
        curTime = 0;
    }

    public T getEvent(long time, boolean createNew)
    {
        if (time < curTime)
        {
            throw new IllegalArgumentException("Time is in the past");
        }

        final ListIterator<T> iter = events.listIterator();
        while (iter.hasNext())
        {
            final T cur = iter.next();
            if (time < cur.time)
            {
                // event precedes current record
                T e = null;
                if (createNew)
                {
                    e = factory.newEvent(time);
                    iter.previous();
                    iter.add(e);
                }
                return e;
            }
            else if (time == cur.time)
            {
                // found existing event record
                return cur;
            }
        }

        // event succeeds last record
        T e = null;
        if (createNew)
        {
            e = factory.newEvent(time);
            iter.add(e);
        }
        return e;
    }

    public List<T> getEventList()
    {
        return events;
    }

    public long getCurrentTime()
    {
        return curTime;
    }

    public T getCurrentEvent()
    {
        if (!events.isEmpty())
        {
            T e = events.getFirst();
            if (e.time == curTime)
            {
                return e;
            }
        }
        return null;
    }

    public void setCurrentTime(long time)
    {
        if (time < curTime)
        {
            throw new IllegalArgumentException("Time is in the past");
        }
        curTime = time;

        Iterator<T> iter = events.iterator();
        while (iter.hasNext())
        {
            EventRecord cur = iter.next();
            if (cur.time < curTime)
            {
                iter.remove();
            }
            else
            {
                break;
            }
        }
    }

    public EventRecord advanceTime()
    {
        while (!events.isEmpty())
        {
            T e = events.getFirst();
            if (e.time > curTime)
            {
                curTime = e.time;
                return e;
            }
            else
            {
                events.removeFirst();
            }
        }
        return null;
    }
}
