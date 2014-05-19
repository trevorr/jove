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

import com.newisys.eventsim.Event;
import com.newisys.eventsim.PulseEvent;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;

/**
 * Implements an unlimited size, blocking FIFO using a SimulationManager.
 * 
 * @param <T> the type of object contained by this mailbox
 * @author Trevor Robinson
 */
public final class Mailbox<T>
{
    private final SimulationManager simManager;
    private final String name;
    private final LinkedList<T> fifo;
    private final Event notEmptyEvent;

    /**
     * Constructs a new mailbox with the given name using the given simulation
     * manager.
     *
     * @param simManager the simulation manager coordinating this mailbox
     * @param name the name of this mailbox (for debugging purposes)
     */
    public Mailbox(SimulationManager simManager, String name)
    {
        this.simManager = simManager;
        this.name = name;
        fifo = new LinkedList<T>();
        notEmptyEvent = new PulseEvent(name + "-NotEmptyEvent");
    }

    private static int serialNo = 1;

    private static synchronized String generateName()
    {
        return "Mailbox" + serialNo++;
    }

    /**
     * Constructs a new mailbox with a generated name using the given simulation
     * manager.
     *
     * @param simManager the simulation manager coordinating this mailbox
     */
    public Mailbox(SimulationManager simManager)
    {
        this(simManager, generateName());
    }

    /**
     * Puts the item into the mailbox in the last position.
     *
     * @param item an item to put in the mailbox
     */
    public void put(T item)
    {
        synchronized (fifo)
        {
            fifo.addLast(item);
        }
        simManager.notifyOf(notEmptyEvent);
    }

    /**
     * Returns the first item in the mailbox. If the mailbox is empty, waits
     * until something is placed in the mailbox. Dequeues the item from the
     * mailbox. If more than one thread is in the getWait call and only one item
     * is placed in the mailbox, only one thread gets the item and all other
     * threads continue to wait.
     *
     * @return the first item in the mailbox
     */
    public T getWait()
    {
        return get(true, false);
    }

    /**
     * Returns the first item in the mailbox. If the mailbox is empty, it
     * returns null. Dequeues the item from the mailbox.
     *
     * @return the first item in the mailbox, or null
     */
    public T getNoWait()
    {
        return get(false, false);
    }

    /**
     * Returns the first item in the mailbox. If the mailbox is empty, waits
     * until something is placed in the mailbox. Does not dequeue the item from
     * the mailbox. If more than one thread is in the peekWait call and only one
     * item is placed in the mailbox, all the thread will get a reference to the
     * item.
     *
     * @return the first item in the mailbox
     */
    public T peekWait()
    {
        return get(true, true);
    }

    /**
     * Returns the first item in the mailbox. If the mailbox is empty, it
     * returns null. Does not dequeue the item from the mailbox.
     *
     * @return the first item in the mailbox, or null
     */
    public T peekNoWait()
    {
        return get(false, true);
    }

    /**
     * Implements getWait, getNoWait, peekWait, peekNoWait.
     *
     * @param wait whether to wait if the mailbox is empty
     * @param peekOnly whether to remove the returned item from the mailbox
     * @return the first item in the mailbox, or null
     */
    private T get(boolean wait, boolean peekOnly)
    {
        T item = null;
        boolean keepTrying = true;

        while (keepTrying)
        {
            synchronized (fifo)
            {
                int size = fifo.size();
                if (size > 0)
                {
                    if (peekOnly)
                    {
                        item = fifo.getFirst();
                    }
                    else
                    {
                        item = fifo.removeFirst();
                    }
                    keepTrying = false;
                }
                else if (wait == false)
                {
                    keepTrying = false;
                }
            }
            if (keepTrying)
            {
                SimulationThread.currentThread().waitFor(notEmptyEvent);
            }
        }

        return item;
    }

    /**
     * Returns the number of items in the mailbox.
     * @return the number of items in the mailbox
     */
    public int size()
    {
        return fifo.size();
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
