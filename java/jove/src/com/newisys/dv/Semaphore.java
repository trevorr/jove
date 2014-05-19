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

import com.newisys.eventsim.Event;
import com.newisys.eventsim.PulseEvent;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;

/**
 * Implements a counting semaphore using a SimulationManager.
 * 
 * @author Trevor Robinson
 */
public final class Semaphore
{
    private final SimulationManager simManager;
    private final String name;
    private int permits;
    private final Event releaseEvent;

    /**
     * Constructs a new semaphore with the given name and initial permit count
     * using the given simulation manager.
     *
     * @param simManager the simulation manager coordinating this semaphore
     * @param name the name of this semaphore (for debugging purposes)
     * @param permits the number of times acquire() will succeed before
     *            release() is called
     */
    public Semaphore(SimulationManager simManager, String name, int permits)
    {
        this.simManager = simManager;
        this.name = name;
        this.permits = permits;
        releaseEvent = new PulseEvent(name + "-ReleaseEvent");
    }

    private static int serialNo = 1;

    private static synchronized String generateName()
    {
        return "Semaphore" + serialNo++;
    }

    /**
     * Constructs a new semaphore with a generated name and the given initial
     * permit count using the given simulation manager.
     *
     * @param simManager the simulation manager coordinating this semaphore
     * @param permits the number of times acquire() will succeed before
     *            release() is called
     */
    public Semaphore(SimulationManager simManager, int permits)
    {
        this(simManager, generateName(), permits);
    }

    /**
     * Acquire one permit from this semaphore, waiting until one is available if
     * necessary.
     */
    public void acquire()
    {
        acquireImpl(1, true);
    }

    /**
     * Acquires the given number of permits from this semaphore, waiting until
     * they are available if necessary. This method is non-greedy, meaning that
     * it will not acquire any permits until the full number of requested
     * permits is available at once. This algorithm prevents deadlock (caused by
     * multiple waiters holding permits while waiting for more) but allows
     * livelock (a thread waiting for multiple permits could be starved by
     * threads waiting for fewer permits), so it should be used with care.
     *
     * @param permits the number of permits to acquire
     */
    public void acquire(int permits)
    {
        acquireImpl(permits, true);
    }

    /**
     * Attempts to acquire one permit from this semaphore. If a permit is
     * available now, this method will acquire it and return true. Otherwise, no
     * permit is acquired and this method returns false.
     *
     * @return a boolean indicating whether a permit was acquired
     */
    public boolean attempt()
    {
        return acquireImpl(1, false);
    }

    /**
     * Attempts to acquire the given number of permits from this semaphore. If
     * all permits are available now, this method will acquire them and return
     * true. Otherwise, no permits are acquired and this method returns false.
     *
     * @param permits the number of permits to acquire
     * @return a boolean indicating whether the permits were acquired
     */
    public boolean attempt(int permits)
    {
        return acquireImpl(permits, false);
    }

    private boolean acquireImpl(int permits, boolean wait)
    {
        boolean acquired = false;

        while (true)
        {
            synchronized (this)
            {
                if (this.permits >= permits)
                {
                    this.permits -= permits;
                    acquired = true;
                    break;
                }
                else if (!wait)
                {
                    break;
                }
            }
            SimulationThread.currentThread().waitFor(releaseEvent);
        }

        return acquired;
    }

    /**
     * Releases one permit for this semaphore, allowing it to be acquired by
     * other threads. Note that no checking is done to ensure that the current
     * thread ever acquired the released permit.
     */
    public void release()
    {
        release(1);
    }

    /**
     * Releases the given number of permits for this semaphore, allowing them to
     * be acquired by other threads. Note that no checking is done to ensure
     * that the current thread ever acquired the released permits.
     *
     * @param permits the number of permits to release
     * @throws IllegalArgumentException if permits is < 0
     */
    public void release(int permits)
    {
        if (permits < 0)
        {
            throw new IllegalArgumentException("Invalid value for permits: "
                + permits);
        }
        synchronized (this)
        {
            this.permits += permits;
        }
        simManager.notifyOf(releaseEvent);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }
}
