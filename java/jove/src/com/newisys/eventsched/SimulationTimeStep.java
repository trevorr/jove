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

package com.newisys.eventsched;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the Verilog stratified event queue for a single
 * simulation time-step.
 * 
 * @author Scott Diesing
 */
public class SimulationTimeStep
{
    private final long simulationTime;
    private final List<SimulationEvent> activeEventQueue = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private final List<SimulationEvent> inactiveEventQueue = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private final List<SimulationEvent> nonblockingAssignUpdateQueue = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private final List<SimulationEvent> monitorEventQueue = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());

    public SimulationTimeStep(long simulationTime)
    {
        this.simulationTime = simulationTime;
    }

    public long getTime()
    {
        return simulationTime;
    }

    public void addEventToActiveQueue(SimulationEvent event)
    {
        activeEventQueue.add(event);
    }

    public void addEventToFrontOfActiveQueue(SimulationEvent event)
    {
        activeEventQueue.add(0, event);
    }

    public void addEventToInactiveQueue(SimulationEvent event)
    {
        inactiveEventQueue.add(event);
    }

    public void addEventToNonblockingAssignUpdateQueue(SimulationEvent event)
    {
        nonblockingAssignUpdateQueue.add(event);
    }

    public void addEventToMonitorQueue(SimulationEvent event)
    {
        monitorEventQueue.add(event);
    }

    /**
     * Process the events in the event queue for this time slice according to
     * the Verilog simulation reference model.
     */
    public void processEvents()
    {
        while ((activeEventQueue.size() + inactiveEventQueue.size()
            + nonblockingAssignUpdateQueue.size() + monitorEventQueue.size()) != 0)
        {
            if (activeEventQueue.size() != 0)
            {
                SimulationEvent event;
                event = activeEventQueue.remove(0);
                event.execute();
                surrender();
            }
            else if (inactiveEventQueue.size() != 0)
            {
                activateEvents(inactiveEventQueue);
            }
            else if (nonblockingAssignUpdateQueue.size() != 0)
            {
                activateEvents(nonblockingAssignUpdateQueue);
            }
            else if (monitorEventQueue.size() != 0)
            {
                activateEvents(monitorEventQueue);
            }
        }
    }

    /**
     * Move the events from the list into the activeEventQueue.
     *
     * @param list source of the events to be moved
     */
    private void activateEvents(List<SimulationEvent> list)
    {
        activeEventQueue.addAll(list);
        list.clear();
    }

    /**
     * Allow the other threads that may be waiting for events to execute.
     */
    private void surrender()
    {
        // do nothing
    }

    /**
     * For testing purposes.
     *
     * @return true if all the queues are empty, else return false
     */
    boolean isEmpty()
    {
        return activeEventQueue.isEmpty() && inactiveEventQueue.isEmpty()
            && nonblockingAssignUpdateQueue.isEmpty()
            && monitorEventQueue.isEmpty();
    }

    boolean remove(SimulationEvent event)
    {
        if (activeEventQueue.remove(event))
        {
            return true;
        }
        if (inactiveEventQueue.remove(event))
        {
            return true;
        }
        if (nonblockingAssignUpdateQueue.remove(event))
        {
            return true;
        }
        if (monitorEventQueue.remove(event))
        {
            return true;
        }
        return false;
    }
}
